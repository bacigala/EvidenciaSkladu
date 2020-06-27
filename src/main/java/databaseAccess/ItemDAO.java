package databaseAccess;

import databaseAccess.CustomExceptions.ConcurrentModificationException;
import databaseAccess.CustomExceptions.UserWarningException;
import domain.CustomAttribute;
import domain.Item;
import domain.ItemMoveLogRecord;
import domain.ItemOfftakeRecord;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Manages communication with DB related to Item.
 * Provides actual item list.
 * Singleton.
 */

public class ItemDAO {
    // singleton
    private ItemDAO() {}
    private static final ItemDAO itemDAO = new ItemDAO();
    public static ItemDAO getInstance() { return itemDAO; }

    // lastly retrieved list of items
    private static ArrayList<Item> itemList = new ArrayList<>();

    /**
     * Reloads possessed list of Items.
     */
    public void reloadItemList() throws Exception {
        if (!Login.getInstance().hasUser()) throw new UserWarningException("Prihláste sa prosím.");

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        ArrayList<Item> newItemList = new ArrayList<>();
        try {
            conn = ConnectionFactory.getInstance().getConnection();
            assert conn != null;
            statement = conn.prepareStatement(
                    "SELECT * FROM item");
            result = statement.executeQuery();
            while (result.next())
                newItemList.add(new Item(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getString("barcode"),
                        result.getInt("min_amount"),
                        result.getInt("cur_amount"),
                        result.getString("unit"),
                        result.getString("note"),
                        result.getInt("category")
                ));
        } catch (SQLException e) {
            throw new UserWarningException("Položky sa nepodarilo načítať.");
        } finally {
            if (result != null) result.close();
            if (statement != null) statement.close();
            if (conn != null) ConnectionFactory.getInstance().releaseConnection(conn);
        }

        itemList = newItemList;
        CategoryDAO.getInstance().reloadCatList();
    }

    /**
     * @return current list of Items
     */
    public ArrayList<Item> getItemList() {
        return itemList;
    }

    /**
     * Removes all cached records (e.g. on logoff).
     */
    public static void dropItemList() {
        itemList.clear();
    }

    /**
     * Tries to create all required records in the database for item supply.
     * @param itemId ID of the supplied item.
     * @param supplyAmount amount of items supplied.
     * @param expiration expiration date of the item supplied.
     */
    public void itemSupply(int itemId, int supplyAmount, LocalDate expiration) throws Exception {
        if (itemId <= 0 || supplyAmount <= 0) throw new IllegalArgumentException();
        if (!Login.getInstance().hasUser()) throw new UserWarningException("Prihláste sa prosím.");

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        Savepoint savepoint1 = null;
        int curAmount;
        int moveId;

        try {
            conn = ConnectionFactory.getInstance().getConnection();
            assert conn != null;
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            savepoint1 = conn.setSavepoint("Savepoint1");

            // load current amount of items present
            statement = conn.prepareStatement(
                    "SELECT * FROM item WHERE id = ?");
            statement.setInt(1, itemId);
            result = statement.executeQuery();
            if (result.next()) {
                curAmount = result.getInt("cur_amount");
            } else {
                throw new UserWarningException("Neexistujúca položka.");
            }

            // increment no. of items present
            statement = conn.prepareStatement(
                    "UPDATE item SET cur_amount = ? WHERE id = ?");
            statement.setInt(1, curAmount + supplyAmount);
            statement.setInt(2, itemId);
            if (statement.executeUpdate() != 1) throw new SQLException();

            // create move record
            statement = conn.prepareStatement(
                    "INSERT INTO move SET account_id = ?, time = NOW()", Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, Login.getInstance().getLoggedUserId());
            if (statement.executeUpdate() != 1) throw new SQLException();
            ResultSet rs = statement.getGeneratedKeys();
            if (rs != null && rs.next()) {
                moveId = rs.getInt(1);
            } else {
                throw new SQLException();
            }

            // link supplied items to move
            statement = conn.prepareStatement(
                    "INSERT INTO move_item SET move_id = ?, item_id = ?, amount = ?, expiration = ?");
            statement.setInt(1, moveId);
            statement.setInt(2, itemId);
            statement.setInt(3, supplyAmount);
            statement.setDate(4, java.sql.Date.valueOf(expiration));
            if (statement.executeUpdate() != 1) throw new SQLException();

            conn.commit();

        } catch (Exception e) {
            assert conn != null;
            conn.rollback(savepoint1);
            throw e;
        } finally {
            if (result != null) result.close();
            if (statement != null) statement.close();
            if (conn != null) ConnectionFactory.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Retrieves all custom attributes of item with 'itemId'.
     * @param itemId ID of the supplied item.
     * @return list of custom attributes.
     */
    public HashSet<CustomAttribute> getItemCustomAttributes(int itemId) throws Exception {
        if (itemId <= 0) throw new IllegalArgumentException();
        if (!Login.getInstance().hasUser()) throw new UserWarningException("Prihláste sa prosím.");

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        HashSet<CustomAttribute> customAttributes = new HashSet<>();

        // read attributes from DB
        try {
            conn = ConnectionFactory.getInstance().getConnection();
            assert conn != null;
            statement = conn.prepareStatement(
                    "SELECT * FROM attribute WHERE item_id = ?");
            statement.setInt(1, itemId);
            result = statement.executeQuery();
            while (result.next())
                customAttributes.add(new CustomAttribute(
                        result.getString("name"),
                        result.getString("content")
                ));
        } catch (SQLException e) {
            throw new UserWarningException("Atribúty položky sa nepodarilo načítať.");
        } finally {
            if (result != null) result.close();
            if (statement != null) statement.close();
            if (conn != null) ConnectionFactory.getInstance().releaseConnection(conn);
        }
        return customAttributes;
    }

    /**
     * Updates item details and inserts / deletes custom attributes.
     * @param originalItem original item.
     * @param newBasicValues new compulsory values for the item
     * @param attributesToAdd new custom attributes (to be inserted)
     * @param attributesToDelete custom attributes to be deleted
     */
    public void itemUpdate(Item originalItem, HashMap<String, String> newBasicValues,
                              HashSet<CustomAttribute> attributesToAdd, HashSet<CustomAttribute> attributesToDelete)
            throws Exception {
        if (originalItem == null || newBasicValues == null || attributesToAdd == null || attributesToDelete == null)
            throw new IllegalArgumentException();
        if (!Login.getInstance().hasUser()) throw new UserWarningException("Prihláste sa prosím.");

        Connection conn = null;
        PreparedStatement statement = null;
        Savepoint savepoint1 = null;
        ResultSet result = null;

        try {
            conn = ConnectionFactory.getInstance().getConnection();
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            savepoint1 = conn.setSavepoint("Savepoint1");

            //check whether the item exists and was not changed concurrently
            statement = conn.prepareStatement(
                    "SELECT * FROM item WHERE id = ?");
            statement.setInt(1, originalItem.getId());
            result = statement.executeQuery();
            if (!result.next()) throw new UserWarningException("Položka, ktorú sa snažíte aktualizovať, neexistuje.");
            if (!result.getString("name").equals(originalItem.getName())
                    || !result.getString("barcode").equals(originalItem.getBarcode())
                    || result.getInt("min_amount") != originalItem.getMinAmount()
                    || !result.getString("unit").equals(originalItem.getUnit())
                    || result.getInt("category") != originalItem.getCategory()
                ) throw new ConcurrentModificationException();

            // update basic info about the item
            statement = conn.prepareStatement(
                    "UPDATE item SET name = ?, barcode = ?, min_amount = ?, unit = ?, category = ? WHERE id = ?");
            statement.setString(1,
                    newBasicValues.containsKey("name") ? newBasicValues.get("name") : originalItem.getName());
            statement.setString(2,
                    newBasicValues.containsKey("barcode") ? newBasicValues.get("barcode") : originalItem.getBarcode());
            statement.setInt(3,
                    newBasicValues.containsKey("min_amount") ?
                            Integer.parseInt(newBasicValues.get("min_amount")) : originalItem.getMinAmount());
            statement.setString(4,
                        newBasicValues.containsKey("unit") ? newBasicValues.get("unit") : originalItem.getUnit());
            statement.setInt(5,
                    newBasicValues.containsKey("category") ?
                            Integer.parseInt(newBasicValues.get("category")) : originalItem.getCategory());
            statement.setInt(6, originalItem.getId());
            if (statement.executeUpdate() != 1) throw new SQLException();

            // create custom attributes records
            for (CustomAttribute newAttribute : attributesToAdd) {
                statement = conn.prepareStatement(
                        "INSERT INTO attribute SET item_id = ?, name = ?, content = ?");
                statement.setInt(1, originalItem.getId());
                statement.setString(2, newAttribute.getName());
                statement.setString(3, newAttribute.getValue());
                if (statement.executeUpdate() != 1) throw new ConcurrentModificationException();
            }

            // remove custom attributes records
            for (CustomAttribute newAttribute : attributesToDelete) {
                statement = conn.prepareStatement(
                        "DELETE FROM attribute WHERE item_id = ? AND name = ? AND content = ?");
                statement.setInt(1, originalItem.getId());
                statement.setString(2, newAttribute.getName());
                statement.setString(3, newAttribute.getValue());
                if (statement.executeUpdate() != 1) throw new ConcurrentModificationException();
            }

            conn.commit();

        } catch (Exception e) {
            assert conn != null;
            conn.rollback(savepoint1);
            throw e;
        } finally {
            if (statement != null) statement.close();
            if (result != null) result.close();
            if (conn != null) ConnectionFactory.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Retrieves transaction log for specified item.
     * @param itemId ID of the requested Item log
     * @return list of log records
     */
    public ArrayList<ItemMoveLogRecord> getItemTransactions(int itemId) throws Exception {
        if (itemId <= 0) throw new IllegalArgumentException();
        if (!Login.getInstance().hasAdmin()) throw new UserWarningException("Prihláste sa prosím.");

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        ArrayList<ItemMoveLogRecord> logRecords = new ArrayList<>();

        try {
            conn = ConnectionFactory.getInstance().getConnection();
            statement = conn.prepareStatement(
                    "SELECT account.name, account.surname, move_item.amount, move.time, move_item.expiration " +
                            "FROM (move_item JOIN move ON (move_item.move_id = move.id)) " +
                            "JOIN account ON (move.account_id = account.id) WHERE move_item.item_id = ? " +
                            "ORDER BY move.time DESC");
            statement.setInt(1, itemId);
            result = statement.executeQuery();
            while (result.next())
                logRecords.add(new ItemMoveLogRecord(
                        result.getDate("time").toString(),
                        ((Integer)result.getInt("amount")).toString(),
                        result.getString("name") + " " + result.getString("surname"),
                        result.getDate("expiration").toString()
                ));
        } finally {
            if (result != null) result.close();
            if (statement != null) statement.close();
            if (conn != null) ConnectionFactory.getInstance().releaseConnection(conn);
        }
        return logRecords;
    }

    /**
     * Request all currently stored varieties (differ in expiration date) of specified item.
     * @param itemId ID of the requested item
     * @param records list to be filled with retrieved varieties
     */
    public void getItemVarieties(int itemId, ObservableList<ItemOfftakeRecord> records) throws Exception {
        if (itemId <= 0 || records == null) throw new IllegalArgumentException();
        if (!Login.getInstance().hasUser()) throw new UserWarningException("Prihláste sa prosím.");

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            conn = ConnectionFactory.getInstance().getConnection();

            statement = conn.prepareStatement(
                    "SELECT SUM(amount) AS sum, expiration " +
                            "FROM `move_item` WHERE item_id = ? GROUP BY expiration HAVING sum > 0");
            statement.setInt(1, itemId);
            result = statement.executeQuery();
            while (result.next())
                records.add(new ItemOfftakeRecord(
                        result.getDate("expiration").toLocalDate(),
                        result.getInt("sum")
                ));

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (result != null) result.close();
            if (statement != null) statement.close();
            if (conn != null) ConnectionFactory.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Tries to 'trash' stated Items (= take off as 'trash user'.)
     * @param item item to be taken
     * @param requestList  list of desired varieties (different expiry dates) of item
     */
    public void itemTrash (Item item, ObservableList<ItemOfftakeRecord> requestList) throws Exception {
        itemOfftake(item, requestList, true);
    }

    /**
     * Tries to 'take off' stated Items in name of loggedIn user.
     * @param item item to be taken
     * @param requestList list of desired varieties (different expiry dates) of item
     */
    public void itemOfftake (Item item, ObservableList<ItemOfftakeRecord> requestList) throws Exception {
        itemOfftake(item, requestList, false);
    }

    private void itemOfftake (Item item, ObservableList<ItemOfftakeRecord> requestList, boolean isTrash)
            throws Exception {
        if (item == null || requestList == null) throw new IllegalArgumentException();
        if (!Login.getInstance().hasUser()) throw new UserWarningException("Prihláste sa prosím.");
        if (isTrash && !Login.getInstance().hasAdmin()) throw new UserWarningException("Nemáte dostatočné oprávnenia.");

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        Savepoint savepoint1 = null;

        try {
            conn = ConnectionFactory.getInstance().getConnection();
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            conn.setAutoCommit(false);
            savepoint1 = conn.setSavepoint("Savepoint1");

            // load current amount of items present
            statement = conn.prepareStatement(
                    "SELECT * FROM item WHERE id = ?");
            statement.setInt(1, item.getId());

            int curAmount;
            result = statement.executeQuery();
            if (result.next()) {
                curAmount = result.getInt("cur_amount");
            } else {
                throw new SQLException();
            }

            // load present varieties - prevent concurrent offtake
            HashMap<LocalDate, Integer> currentRecords = new HashMap<>();
            statement = conn.prepareStatement(
                    "SELECT SUM(amount) AS sum, expiration " +
                            "FROM `move_item` WHERE item_id = ? GROUP BY expiration HAVING sum > 0");
            statement.setInt(1, item.getId());
            result = statement.executeQuery();
            while (result.next())
                currentRecords.put(
                        result.getDate("expiration").toLocalDate(),
                        result.getInt("sum")
                );

            // check whether all of requested takeoffs can be fulfilled
            int noOfRequestedItems = 0;
            for (ItemOfftakeRecord request : requestList) {
                if (!currentRecords.containsKey(request.getExpiration())
                        || currentRecords.get(request.getExpiration()) < Integer.parseInt(request.getRequestedAmount())) {
                    // a request cannot be fulfilled -> fail
                    throw new UserWarningException("Požadovaná kombinácia (už) nie je dostupná.");
                }
                noOfRequestedItems += Integer.parseInt(request.getRequestedAmount());
            }

            // ALL REQUESTS CAN BE FULFILLED
            // decrement no. of items present
            statement = conn.prepareStatement(
                    "UPDATE item SET cur_amount = ? WHERE id = ?");
            statement.setInt(1, curAmount - noOfRequestedItems);
            statement.setInt(2, item.getId());
            if (statement.executeUpdate() != 1) throw new SQLException();

            // create move record
            int moveId;
            statement = conn.prepareStatement(
                    "INSERT INTO move SET account_id = ?, time = ?", Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, isTrash ? 1 : Login.getInstance().getLoggedUserId());
            statement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            if (statement.executeUpdate() != 1) throw new SQLException();
            ResultSet rs = statement.getGeneratedKeys();
            if (rs != null && rs.next()) {
                moveId = rs.getInt(1);
            } else {
                throw new SQLException();
            }

            // create move_item records, link them to move record
            for (ItemOfftakeRecord request : requestList) {
                if (Integer.parseInt(request.getRequestedAmount()) <= 0) continue;
                statement = conn.prepareStatement(
                        "INSERT INTO move_item SET move_id = ?, item_id = ?, amount = ?, expiration = ?");
                statement.setInt(1, moveId);
                statement.setInt(2, item.getId());
                statement.setInt(3, -Integer.parseInt(request.getRequestedAmount()));
                statement.setDate(4, java.sql.Date.valueOf(request.getExpiration()));
                if (statement.executeUpdate() != 1) throw new SQLException();
            }

            conn.commit();

        } catch (Exception e) {
            assert conn != null;
            conn.rollback(savepoint1);
            throw e;
        } finally {
            if (result != null) result.close();
            if (statement != null) statement.close();
            if (conn != null) ConnectionFactory.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Inserts new item with custom attributes.
     * @param newItem item to be inserted
     * @param attributesToAdd new custom attributes
     */
    public void itemInsert(Item newItem, HashSet<CustomAttribute> attributesToAdd) throws Exception {
        if (!Login.getInstance().hasAdmin()) throw new UserWarningException("Prihláste sa prosím.");

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        Savepoint savepoint1 = null;

        try {
            conn = ConnectionFactory.getInstance().getConnection();
            conn.setAutoCommit(false);
            savepoint1 = conn.setSavepoint("Savepoint1");

            // insert basic info about the item
            statement = conn.prepareStatement(
                    "INSERT INTO item SET name = ?, barcode = ?, min_amount = ?, unit = ?, category = ?",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, newItem.getName());
            statement.setString(2, newItem.getBarcode());
            statement.setInt(3, newItem.getMinAmount());
            statement.setString(4, newItem.getUnit());
            statement.setInt(5, newItem.getCategory());
            if (statement.executeUpdate() != 1) throw new SQLException();

            // get ID of the new Item
            int itemId;
            result = statement.getGeneratedKeys();
            if (result != null && result.next()) {
                itemId = result.getInt(1);
            } else {
                throw new SQLException();
            }

            // create custom attributes records
            for (CustomAttribute newAttribute : attributesToAdd) {
                statement = conn.prepareStatement(
                        "INSERT INTO attribute SET item_id = ?, name = ?, content = ?");
                statement.setInt(1, itemId);
                statement.setString(2, newAttribute.getName());
                statement.setString(3, newAttribute.getValue());
                if (statement.executeUpdate() != 1) throw new SQLException();
            }

            conn.commit();

        } catch (Exception e) {
            assert conn != null;
            conn.rollback(savepoint1);
            throw e;
        } finally {
            if (result != null) result.close();
            if (statement != null) statement.close();
            if (conn != null) ConnectionFactory.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Deletes item from DB.
     * @param item item to be deleted.
     */
    public static void itemDelete(Item item) throws Exception {
        if (item == null) throw new IllegalArgumentException();
        if (!Login.getInstance().hasAdmin()) throw new UserWarningException("Prihláste sa prosím.");

        Connection conn = null;
        PreparedStatement statement = null;
        Savepoint savepoint1 = null;

        try {
            conn = ConnectionFactory.getInstance().getConnection();
            savepoint1 = conn.setSavepoint("Savepoint1");

            statement = conn.prepareStatement("DELETE FROM item WHERE id = ?");
            statement.setInt(1, item. getId());
            if (statement.executeUpdate() != 1) throw new SQLException();
        } catch (Exception e) {
            assert conn != null;
            conn.rollback(savepoint1);
            throw e;
        } finally {
            if (statement != null) statement.close();
            if (conn != null) ConnectionFactory.getInstance().releaseConnection(conn);
        }
    }

}
