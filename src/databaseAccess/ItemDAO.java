package databaseAccess;

import domain.CustomAttribute;
import domain.Item;
import domain.ItemMoveLogRecord;
import domain.ItemOfftakeRecord;
import javafx.collections.ObservableList;

import java.io.IOException;
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
     * @return true on success
     */
    public boolean reloadItemList() {
        if (!Login.getInstance().hasUser()) return false;

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        ArrayList<Item> newItemList = new ArrayList<>();
        boolean exceptionThrown = false;
        try {
            conn = ConnectionFactory.getInstance().getConnection();
            assert conn != null;
            statement = conn.prepareStatement(
                    "SELECT * FROM item");
            result = statement.executeQuery();
            while (result.next()) {
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
            }
        } catch (SQLException e) {
            exceptionThrown = true;
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) ConnectionFactory.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                exceptionThrown = true;
            }
        }
        if (exceptionThrown) return false;
        itemList = newItemList;

        return CategoryDAO.getInstance().reloadCatList();
    }

    /**
     * @return current list of Items.
     */
    public ArrayList<Item> getItemList() {
        return itemList;
    }

    // remove all cached records (on logoff)
    static void dropItemList() {
        itemList.clear();
    }

    /**
     * Tries to create all required records in the database for item supply.
     * @param itemId ID of the supplied item.
     * @param supplyAmount amount of items supplied.
     * @param expiration expiration date of the item supplied.
     * @return true if supply was successful.
     */
    public boolean itemSupply(int itemId, int supplyAmount, LocalDate expiration) {
        if (!Login.getInstance().hasUser()) return false;

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        Savepoint savepoint1 = null;
        int curAmount;
        int moveId;
        boolean success = true;

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
                throw new SQLException();
            }

            // increment no. of items present
            statement = conn.prepareStatement(
                    "UPDATE item SET cur_amount = ? WHERE id = ?");
            statement.setInt(1, curAmount + supplyAmount);
            statement.setInt(2, itemId);
            if (statement.executeUpdate() != 1) throw new SQLException();

            // create move record
            statement = conn.prepareStatement(
                    "INSERT INTO move SET account_id = ?, time = ?", Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, Login.getInstance().getLoggedUserId());
            statement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
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
            e.printStackTrace();
            try {
                assert conn != null;
                conn.rollback(savepoint1);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            success = false;
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) ConnectionFactory.getInstance().releaseConnection(conn);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return success;
    }

    /**
     * Retrieves all custom attributes of item with 'itemId'.
     * @param itemId ID of the supplied item.
     * @return list of custom attributes, null on fail.
     */
    public HashSet<CustomAttribute> getItemCustomAttributes(int itemId) {
        if (!Login.getInstance().hasUser()) return null;

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        // READ ATTRIBUTES FROM DB
        HashSet<CustomAttribute> customAttributes = new HashSet<>();
        try {
            conn = ConnectionFactory.getInstance().getConnection();
            assert conn != null;
            statement = conn.prepareStatement(
                    "SELECT * FROM attribute WHERE item_id = ?");
            statement.setInt(1, itemId);
            result = statement.executeQuery();
            while (result.next()) {
                customAttributes.add(new CustomAttribute(
                        result.getString("name"),
                        result.getString("content")
                ));
            }
        } catch (SQLException e) {
            customAttributes = null;
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) ConnectionFactory.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return customAttributes;
    }

    /**
     * Updates item details and inserts / deletes custom attributes.
     * @param originalItem       original item.
     * @param newBasicValues     new compulsory values for the item
     * @param attributesToAdd    new custom attributes (to be inserted)
     * @param attributesToDelete custom attributes to be deleted
     * @return true on success.
     */
    public boolean itemUpdate(Item originalItem, HashMap<String, String> newBasicValues,
                              HashSet<CustomAttribute> attributesToAdd, HashSet<CustomAttribute> attributesToDelete) {
        if (!Login.getInstance().hasUser()) return false;
        Connection conn = null;
        PreparedStatement statement = null;
        Savepoint savepoint1 = null;
        boolean success = true;

        try {
            conn = ConnectionFactory.getInstance().getConnection();
            assert conn != null;
            conn.setAutoCommit(false);
            savepoint1 = conn.setSavepoint("Savepoint1");

            //todo: check whether the item was not changed concurrently

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
                if (statement.executeUpdate() != 1) throw new SQLException();
            }

            // remove custom attributes records
            for (CustomAttribute newAttribute : attributesToDelete) {
                statement = conn.prepareStatement(
                        "DELETE FROM attribute WHERE item_id = ? AND name = ? AND content = ?");
                statement.setInt(1, originalItem.getId());
                statement.setString(2, newAttribute.getName());
                statement.setString(3, newAttribute.getValue());
                if (statement.executeUpdate() != 1) throw new SQLException();
            }

            conn.commit();

        } catch (Exception e) {
            e.printStackTrace();
            try {
                assert conn != null;
                conn.rollback(savepoint1);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            success = false;
        } finally {
            try {
                if (statement != null) statement.close();
                if (conn != null) ConnectionFactory.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    /**
     * Retrieves transaction log for specified item.
     * @param itemId ID of the requested Item log.
     * @return list of log records.
     */
    public ArrayList<ItemMoveLogRecord> getItemTransactions(int itemId) {
        if (!Login.getInstance().hasAdmin()) return null;

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        ArrayList<ItemMoveLogRecord> logRecords = new ArrayList<>();

        try {
            conn = ConnectionFactory.getInstance().getConnection();
            assert conn != null;
            statement = conn.prepareStatement(
                    "SELECT account.name, account.surname, move_item.amount, move.time, move_item.expiration " +
                            "FROM (move_item JOIN move ON (move_item.move_id = move.id)) " +
                            "JOIN account ON (move.account_id = account.id) WHERE move_item.item_id = ? " +
                            "ORDER BY move.time DESC");
            statement.setInt(1, itemId);
            result = statement.executeQuery();
            while (result.next()) {
                logRecords.add(new ItemMoveLogRecord(
                        result.getDate("time").toString(),
                        ((Integer)result.getInt("amount")).toString(),
                        result.getString("name") + " " + result.getString("surname"),
                        result.getDate("expiration").toString()
                ));
            }
        } catch (SQLException e) {
            logRecords = null;
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) ConnectionFactory.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return logRecords;
    }

    /**
     * Request all currently stored varieties of specified item.
     * @param itemId ID of the requested item.
     * @param records list to be filled with retrieved varieties.
     * @return true on success.
     */
    public boolean getItemVarieties(int itemId, ObservableList<ItemOfftakeRecord> records) {
        if (!Login.getInstance().hasUser()) return false;

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        boolean success = true;

        try {
            conn = ConnectionFactory.getInstance().getConnection();

            // load current amount of items present
            statement = conn.prepareStatement(
                    "SELECT SUM(amount) AS sum, expiration " +
                            "FROM `move_item` WHERE item_id = ? GROUP BY expiration HAVING sum > 0");
            statement.setInt(1, itemId);
            result = statement.executeQuery();
            while (result.next()) {
                records.add(new ItemOfftakeRecord(
                        result.getDate("expiration").toLocalDate(),
                        result.getInt("sum")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) ConnectionFactory.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    /**
     * Tries to 'trash' stated Items as 'trash user'.
     * @param item         item to be taken.
     * @param requestList  list of desired varieties (different expiry dates) of item.
     * @return true on success.
     */
    public boolean itemTrash (Item item, ObservableList<ItemOfftakeRecord> requestList) {
        return itemOfftake (item, requestList, true);
    }

    /**
     * Tries to 'take off' stated Items in name of loggedIn user.
     * @param item         item to be taken.
     * @param requestList  list of desired varieties (different expiry dates) of item.
     * @return true on success.
     */
    public boolean itemOfftake (Item item, ObservableList<ItemOfftakeRecord> requestList) {
        return itemOfftake (item, requestList, false);
    }

    private boolean itemOfftake (Item item, ObservableList<ItemOfftakeRecord> requestList, boolean isTrash) {
        if (!Login.getInstance().hasUser()) return false;
        if (isTrash && !Login.getInstance().hasAdmin()) return false; // only admin can trash
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        Savepoint savepoint1 = null;
        boolean success = true;

        try {
            conn = ConnectionFactory.getInstance().getConnection();
            assert conn != null;
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

            // load content of DB once again - prevent concurrent offtake
            HashMap<LocalDate, Integer> currentRecords = new HashMap<>();
            statement = conn.prepareStatement(
                    "SELECT SUM(amount) AS sum, expiration " +
                            "FROM `move_item` WHERE item_id = ? GROUP BY expiration HAVING sum > 0");
            statement.setInt(1, item.getId());
            result = statement.executeQuery();
            while (result.next()) {
                currentRecords.put(
                        result.getDate("expiration").toLocalDate(),
                        result.getInt("sum")
                );
            }

            // check whether all of requested takeoffs can be fulfilled
            int noOfRequestedItems = 0;
            for (ItemOfftakeRecord request : requestList) {
                if (!currentRecords.containsKey(request.getExpiration())
                        || currentRecords.get(request.getExpiration()) < Integer.parseInt(request.getRequestedAmount())) {
                    // a request cannot be fulfilled -> fail
                    throw new IOException(); // todo: special FAIL exception?
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

        } catch (Throwable e) {
            e.printStackTrace();
            try {
                assert conn != null;
                conn.rollback(savepoint1);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            success = false;
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) ConnectionFactory.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    /**
     * Inserts new item with custom attributes.
     * @param newItem           item to be inserted
     * @param attributesToAdd   new custom attributes
     * @return true on success.
     */
    public boolean itemInsert(Item newItem, HashSet<CustomAttribute> attributesToAdd) {
        if (!Login.getInstance().hasAdmin()) return false;
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        Savepoint savepoint1 = null;
        boolean success = true;

        try {
            conn = ConnectionFactory.getInstance().getConnection();
            assert conn != null;
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
            e.printStackTrace();
            try {
                assert conn != null;
                conn.rollback(savepoint1);
            } catch (SQLException ex) {
                e.printStackTrace();
            }
            success = false;
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) ConnectionFactory.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    /**
     * Deletes item  from DB.
     * @param item  item to be deleted.
     * @return true on success.
     */
    public static boolean itemDelete(Item item) {
        if (!Login.getInstance().hasAdmin()) return false;
        Connection conn = null;
        PreparedStatement statement = null;
        Savepoint savepoint1 = null;
        boolean success = true;

        try {
            conn = ConnectionFactory.getInstance().getConnection();
            assert conn != null;
            savepoint1 = conn.setSavepoint("Savepoint1");

            statement = conn.prepareStatement("DELETE FROM item WHERE id = ?");
            statement.setInt(1, item. getId());

            if (statement.executeUpdate() != 1) throw new SQLException();

            // todo: v tabulke 'move' mozno zostal redundantny zaznam ak to bol prave zmazany posledny 'move_item' zaznam...

        } catch (Exception e) {
            e.printStackTrace();
            try {
                assert conn != null;
                conn.rollback(savepoint1);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            success = false;
        } finally {
            try {
                if (statement != null) statement.close();
                if (conn != null) ConnectionFactory.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return success;
    }

}
