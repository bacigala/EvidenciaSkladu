
package databaseAccess;

import javafx.collections.ObservableList;

import java.awt.image.AreaAveragingScaleFilter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class responsible for all interaction with the database.
 * Singleton.
 */

public class QueryHandler {
    // singleton instance
    private static final QueryHandler queryHandler = new QueryHandler();

    // logged in user info
    private boolean loggedUserAdmin = false;
    private String loggedUserUsername = "";
    private String loggedUserName = "";
    private int loggedUserId = 0;

    // connection details
    private String databaseIp = "192.168.0.10";
    private String databasePort = "3306";
    private String databaseName = "zubardb";
    private String databaseUsername;
    private String databasePassword;

    // last retrieved list of items and map of categories
    private ArrayList<Item> itemList = new ArrayList<>();
    private HashMap<Integer, Category> categoryMap = new HashMap<>();


    /**
     * Empty constructor - singleton class.
     */
    private QueryHandler() {}

    /**
     * @return the only singleton instance.
     */
    public static QueryHandler getInstance() {
        return queryHandler;
    }

    /**
     * @return connection based on current connection details.
     */
    private Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:mysql://"
                    + databaseIp + ":" + databasePort + "/"
                    + databaseName, databaseUsername, databasePassword);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * @return true if valid connection details are present.
     */
    public boolean hasConnectionDetails() {
        Connection connection = getConnection();
        boolean result = connection != null;
        if (result) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Setups connection with basic database privileges. (additional protection)
     * @return true if logged in.
     */
    public boolean setBasicUserConnectionDetails() {
        databaseUsername = "basic-user";
        databasePassword = "CwJNF7zJciaxMY3v";
        return hasConnectionDetails();
    }

    public boolean setBasicUserConnectionDetails(String ip, String port) {
        databaseIp = ip;
        databasePort = port;
        return setBasicUserConnectionDetails();
    }

    /**
     * Setups connection with admin database privileges.
     * @return true if logged in.
     */
    private boolean setAdminUserConnectionDetails() {
        databaseUsername = "admin-user";
        databasePassword = "scfAT4nHm5MKJu9D";
        return hasConnectionDetails();
    }

    public String getDatabaseIp() {
        return databaseIp;
    }

    public String getDatabasePort() {
        return databasePort;
    }

    /**
     * Verifies username and password.
     * @param username Username to be tested.
     * @param password Password to be tested.
     * @return true if username and password are valid.
     */
    public boolean logIn(String username, String password) {
        if (loggedUserId > 0) return false;       
        
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;       
        
        try {   
            conn = getConnection();
            assert conn != null;
            statement = conn.prepareStatement("SELECT * FROM account WHERE login = ? AND password = sha2(?,256)");
            statement.setString(1, username);
            statement.setString(2, password);
            result = statement.executeQuery();
            if (result.next()) {
                loggedUserName = result.getString("name") + " "
                        + result.getString("surname");
                loggedUserId = result.getInt("id");
                loggedUserAdmin = result.getBoolean("admin");
                loggedUserUsername = username;
            } else {
                return false;
            }            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        // if logged in user is an admin, switch to ADMIN database access
        if (loggedUserAdmin) {
            return setAdminUserConnectionDetails();
        } else {
            return setBasicUserConnectionDetails();
        }
    }

    /**
     * Removes information about currently logged user + sets BASIC database user for further database access.
     */
    public void logOut() {
        setBasicUserConnectionDetails();
        loggedUserAdmin = false;
        loggedUserUsername = "";
        loggedUserName = "";
        loggedUserId = 0;

        // delete login-required access content
        itemList.clear();
        categoryMap.clear();
    }

    /**
     * @return currently logged user ID, 0 if no user is present.
     */
    public int getLoggedUserId() {
        return loggedUserId;
    }

    // returns full name of currently logged user
    public String getLoggedUserName() {
        return loggedUserName;
    }

    // returns username of currently logged in uer
    public String getLoggedUserUsername() {
        return loggedUserUsername;
    }
    
    // returns true if verified user is logged in
    public boolean hasUser() {
        return loggedUserId > 0;
    }

    // returns true if verified admin is logged in
    public boolean hasAdmin() {
        return hasUser() && loggedUserAdmin;
    }

    // reloads list of Items from database
    public boolean reloadItemList() {      
        if (!hasConnectionDetails() || !hasUser()) return false;
                 
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;      

        // RELOAD OF ITEMS
        ArrayList<Item> newItemList = new ArrayList<>();
        try {   
            conn = getConnection();
            assert conn != null;
            statement = conn.prepareStatement(
                    "SELECT * FROM item");
            result = statement.executeQuery();
            while (result.next()) {
                Item item = new Item(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getString("barcode"),
                        result.getInt("min_amount"),
                        result.getInt("cur_amount"),
                        result.getString("unit"),
                        result.getString("note"),
                        result.getInt("category")                  
                        );                                       
                newItemList.add(item);                   
            }             
        } catch (SQLException e) {
            return false;
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                return false;
            }
        }
        itemList = newItemList;
                
        // RELOAD OF CATEGORIES
        HashMap<Integer,Category> newCategoryMap = new HashMap<>();
        try {   
            conn = getConnection();            
            statement = conn.prepareStatement(
                    "SELECT * FROM category");
            result = statement.executeQuery();
            while (result.next()) {
                Category cat = new Category(
                        result.getInt("id"),
                        result.getInt("subcat_of"),
                        result.getString("name"),
                        result.getString("color"),
                        result.getString("note")                                         
                        );                                       
                newCategoryMap.put(cat.getId(),cat);                   
            }             
        } catch (SQLException e) {
            return false;
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                return false;
            }
        }
        categoryMap = newCategoryMap;     
                
        return true;        
    }
    
    // returns lastly retrieved list of Items form database
    public ArrayList<Item> getItemList() {
        return itemList;
    }
    
    // returns lastly retrieved list of Categories form database
    public HashMap<Integer,Category> getCategoryMap() {
        return categoryMap;
    }

    /**
     * Tries to create all required records in the database for item supply.
     * @param itemId ID of the supplied item.
     * @param supplyAmount amount of items supplied.
     * @param expiration expiration date of the item supplied.
     * @return true if supply was successful.
     */
    public boolean itemSupply(int itemId, int supplyAmount, LocalDate expiration) {
        if (!hasConnectionDetails() || !hasUser()) return false;
                 
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null; 
        Savepoint savepoint1 = null;
        int curAmount;
        int moveId;
        
        try {   
            conn = getConnection();
            assert conn != null;
            conn.setAutoCommit(false);
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
            statement.setInt(1, getLoggedUserId());
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
        
        } catch (Throwable e) {
            e.printStackTrace();
            try {
                assert conn != null;
                conn.rollback(savepoint1);
            } catch (SQLException ex) {
                Logger.getLogger(QueryHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return true;       
    }

    /**
     * Retrieves all custom attributes of item with 'itemId'.
     * @param itemId ID of the supplied item.
     * @return list of custom attributes.
     */
    public HashSet<CustomAttribute> getItemCustomAttributes(int itemId) {
        if (!hasConnectionDetails() || !hasUser()) return null;

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        // READ ATTRIBUTES FROM DB
        HashSet<CustomAttribute> customAttributes = new HashSet<>();
        try {
            conn = getConnection();
            assert conn != null;
            statement = conn.prepareStatement(
                    "SELECT * FROM attribute WHERE item_id = ?");
            statement.setInt(1, itemId);
            result = statement.executeQuery();
            while (result.next()) {
                CustomAttribute customAttribute =  new CustomAttribute(
                        result.getString("name"),
                        result.getString("content")
                );
                customAttributes.add(customAttribute);
            }
        } catch (SQLException e) {
            return null;
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                return null;
            }
        }
       return customAttributes;
    }

    /**
     * Updates item details and inserts / deletes custom attributes.
     * @param originalItem               original item.
     * @param newBasicValues     new compulsory values for the item
     * @param attributesToAdd    new custom attributes (to be inserted)
     * @param attributesToDelete custom attributes to be deleted
     *
     * @return true on success.
     */
    public boolean itemUpdate(Item originalItem, HashMap<String, String> newBasicValues,
                              HashSet<CustomAttribute> attributesToAdd, HashSet<CustomAttribute> attributesToDelete) {
        if (!hasConnectionDetails() || !hasUser()) return false;
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        Savepoint savepoint1 = null;

        try {
            conn = getConnection();
            assert conn != null;
            conn.setAutoCommit(false);
            savepoint1 = conn.setSavepoint("Savepoint1");

            // update basic info about the item
            statement = conn.prepareStatement(
                    "UPDATE item SET name = ?, barcode = ?, min_amount = ?, unit = ?, category = ? WHERE id = ?");
            if (newBasicValues.containsKey("name")) {
                statement.setString(1, newBasicValues.get("name"));
            } else {
                statement.setString(1, originalItem.getName());
            }
            if (newBasicValues.containsKey("barcode")) {
                statement.setString(2, newBasicValues.get("barcode"));
            } else {
                statement.setString(2, originalItem.getBarcode());
            }
            if (newBasicValues.containsKey("min_amount")) {
                statement.setInt(3, Integer.parseInt(newBasicValues.get("min_amount")));
            } else {
                statement.setInt(3, originalItem.getMinAmount());
            }
            if (newBasicValues.containsKey("unit")) {
                statement.setString(4, newBasicValues.get("unit"));
            } else {
                statement.setString(4, originalItem.getUnit());
            }
            if (newBasicValues.containsKey("category")) {
                statement.setInt(5, Integer.parseInt(newBasicValues.get("category")));
            } else {
                statement.setInt(5, originalItem.getCategory());
            }
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

        } catch (Throwable e) {
            e.printStackTrace();
            try {
                assert conn != null;
                conn.rollback(savepoint1);
            } catch (SQLException ex) {
                Logger.getLogger(QueryHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * Retrieves transaction log for specified item in format of ArrayList<ItemMoveLogRecord>.
     * @param itemId ID of the requested Item log.
     * @return list of log records.
     */
    public ArrayList<ItemMoveLogRecord> getItemTransactions(int itemId) {
        if (!hasConnectionDetails() || !hasUser()) return null;

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        // READ LOG FROM DB
        ArrayList<ItemMoveLogRecord> logRecords = new ArrayList<>();
        try {
            conn = getConnection();
            assert conn != null;
            statement = conn.prepareStatement(
                    "SELECT account.name, account.surname, move_item.amount, move.time " +
                            "FROM (move_item JOIN move ON (move_item.move_id = move.id)) " +
                            "JOIN account ON (move.account_id = account.id) WHERE move_item.item_id = ? " +
                            "ORDER BY move.time DESC");
            statement.setInt(1, itemId);
            result = statement.executeQuery();
            while (result.next()) {
                ItemMoveLogRecord logRecord =  new ItemMoveLogRecord(
                        result.getDate("time").toString(),
                        ((Integer)result.getInt("amount")).toString(),
                        result.getString("name") + " " + result.getString("surname")
                );
                logRecords.add(logRecord);
            }
        } catch (SQLException e) {
            return null;
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                logRecords = null;
            }
        }
        return logRecords;
    }

    /**
     * Request all currently stored varieties of specified item.
     * @param itemId ID of the requested item.
     * @return true on success.
     */
    public boolean getItemOfftakeRecords(int itemId, ObservableList<ItemOfftakeRecord> records) {
        if (!hasConnectionDetails() || !hasUser()) return false;

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        Savepoint savepoint1 = null;
        int moveId;

        try {
            conn = getConnection();
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            assert conn != null;
            conn.setAutoCommit(false);
            savepoint1 = conn.setSavepoint("Savepoint1");

            // load current amount of items present
            statement = conn.prepareStatement(
                    "SELECT SUM(amount) AS sum, expiration FROM `move_item` WHERE item_id = ? GROUP BY expiration HAVING sum > 0");
            statement.setInt(1, itemId);
            result = statement.executeQuery();
            while (result.next()) {
                ItemOfftakeRecord record =  new ItemOfftakeRecord(
                        result.getDate("expiration").toLocalDate(),
                        result.getInt("sum")
                );
                records.add(record);
            }
            conn.commit();

        } catch (Throwable e) {
            e.printStackTrace();
            try {
                assert conn != null;
                conn.rollback(savepoint1);
            } catch (SQLException ex) {
                Logger.getLogger(QueryHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * Tries 'take off' stated Items.
     * @param item         item to be taken.
     * @param requestList  list of desired expiry dates.
     *
     * @return true on success.
     */
    public boolean itemOfftake (Item item, ObservableList<ItemOfftakeRecord> requestList) {
        if (!hasConnectionDetails() || !hasUser()) return false;
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        Savepoint savepoint1 = null;

        try {
            conn = getConnection();
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            assert conn != null;
            conn.setAutoCommit(false);
            savepoint1 = conn.setSavepoint("Savepoint1");

            int noOfRequestedItems = 0;

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

            // load content of DB once again
            HashMap<LocalDate, Integer> currentRecords = new HashMap<>();
            statement = conn.prepareStatement(
                    "SELECT SUM(amount) AS sum, expiration FROM `move_item` WHERE item_id = ? GROUP BY expiration HAVING sum > 0");
            statement.setInt(1, item.getId());
            result = statement.executeQuery();
            while (result.next()) {
                ItemOfftakeRecord record =  new ItemOfftakeRecord(
                        result.getDate("expiration").toLocalDate(),
                        result.getInt("sum")
                );
                currentRecords.put(record.getExpiration(), record.getCurrentAmount());
            }

            // check whether all of requested takeoffs can be fulfilled
            for (ItemOfftakeRecord request : requestList) {
                if (!currentRecords.containsKey(request.getExpiration())
                        || currentRecords.get(request.getExpiration()) < Integer.parseInt(request.getRequestedAmount())) {
                    throw new IOException(); // todo: special FAIL exception
                }
                noOfRequestedItems += Integer.parseInt(request.getRequestedAmount());
            }

            // vsetko OK -> vytvorime zaznamy pre vyber


            // decrement no. of items present
            statement = conn.prepareStatement(
                    "UPDATE item SET cur_amount = ? WHERE id = ?");
            statement.setInt(1, curAmount - noOfRequestedItems);
            statement.setInt(2, item.getId());
            if (statement.executeUpdate() != 1) throw new SQLException();

            // create move record
            int moveId = 0;
            statement = conn.prepareStatement(
                    "INSERT INTO move SET account_id = ?, time = ?", Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, getLoggedUserId());
            statement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            if (statement.executeUpdate() != 1) throw new SQLException();
            ResultSet rs = statement.getGeneratedKeys();
            if (rs != null && rs.next()) {
                moveId = rs.getInt(1);
            } else {
                throw new SQLException();
            }

            // link supplied items to move
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
                Logger.getLogger(QueryHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * Retrieves all expiry warnings according to set date horizont.
     * @param
     * @return list of warnings.
     */
    public ArrayList<ExpiryDateWarningRecord> getExpiryDateWarnings() {
        // todo: implement this method
        return null;

//        if (!hasConnectionDetails() || !hasUser()) return null;
//
//        Connection conn = null;
//        PreparedStatement statement = null;
//        ResultSet result = null;
//
//        // READ WARNINGS FROM DB
//        ArrayList<ExpiryDateWarningRecord> records = new ArrayList<>();
//        try {
//            conn = getConnection();
//            assert conn != null;
//            statement = conn.prepareStatement(
//                    "SELECT account.name, account.surname, move_item.amount, move.time " +
//                            "FROM (move_item JOIN move ON (move_item.move_id = move.id)) " +
//                            "JOIN account ON (move.account_id = account.id) WHERE move_item.item_id = ? " +
//                            "ORDER BY move.time DESC");
//            statement.setInt(1, itemId);
//            result = statement.executeQuery();
//            while (result.next()) {
//                ExpiryDateWarningRecord logRecord =  new ExpiryDateWarningRecord(
//                        result.getDate("time").toString(),
//                        ((Integer)result.getInt("amount")).toString(),
//                        result.getString("name") + " " + result.getString("surname")
//                );
//                records.add(logRecord);
//            }
//        } catch (SQLException e) {
//            return null;
//        } finally {
//            try {
//                if (result != null) result.close();
//                if (statement != null) statement.close();
//                if (conn != null) conn.close();
//            } catch (SQLException e) {
//                records = null;
//            }
//        }
//        return records;
    }

    /**
     * Return current records in DB table 'account'.
     * @param accounts - list to be filled with retrieved records.
     * @return true in success.
     */
    public boolean getAccounts(ObservableList<Account> accounts) {
        if (!hasConnectionDetails() || !hasUser()) return false;
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        Savepoint savepoint1 = null;

        try {
            conn = getConnection();
            assert conn != null;
            // load current records of accounts
            statement = conn.prepareStatement(
                    "SELECT id, name, surname, login, admin FROM `account` ORDER BY admin DESC, surname ASC");
            result = statement.executeQuery();
            while (result.next()) {
                Account account =  new Account(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getString("surname"),
                        result.getString("login"),
                        "", // password in not retrieved from DB.
                        result.getBoolean("admin")
                );
                accounts.add(account);
            }

        } catch (Throwable e) {
            e.printStackTrace();
            try {
                assert conn != null;
                conn.rollback(savepoint1);
            } catch (SQLException ex) {
                Logger.getLogger(QueryHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * Tries to add a new user account.
     * @param newAccount - account to be added.
     * @return true in success.
     */
    public boolean createAccount(Account newAccount) {
        if (!hasConnectionDetails() || !hasUser()) return false;

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        Savepoint savepoint1 = null;

        try {
            conn = getConnection();
            assert conn != null;
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            savepoint1 = conn.setSavepoint("Savepoint1");

            // verify whether username is not occupied
            statement = conn.prepareStatement(
                    "SELECT * FROM account WHERE login = ?");
            statement.setString(1, newAccount.getLogin());
            result = statement.executeQuery();
            if (result.next()) {
                // todo: login je obsadeny
                System.out.println("login je obsadeny");
                throw new SQLException();
            }

            // create new account
            statement = conn.prepareStatement(
                    "INSERT INTO account SET name = ?, surname = ?, login = ?, password = sha2(?,256), admin = ?");
            statement.setString(1, newAccount.getName());
            statement.setString(2, newAccount.getSurname());
            statement.setString(3, newAccount.getLogin());
            statement.setString(4, newAccount.getPassword());
            statement.setBoolean(5, newAccount.isAdmin());

            if (statement.executeUpdate() != 1) {
                // todo: nepodarilo sa vytvorit konto
                System.out.println("nepodarilo sa vytvorit nove konto");
                throw new SQLException();
            }

            conn.commit();

        } catch (Throwable e) {
            e.printStackTrace();
            try {
                assert conn != null;
                conn.rollback(savepoint1);
            } catch (SQLException ex) {
                Logger.getLogger(QueryHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * Tries to modify a user account.
     * @param targetAccount - account to be modified.
     * @return true in success.
     */
    public boolean modifyAccount(Account targetAccount) {
        if (!hasConnectionDetails() || !hasUser()) return false;

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        Savepoint savepoint1 = null;

        try {
            conn = getConnection();
            assert conn != null;
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            savepoint1 = conn.setSavepoint("Savepoint1");

            // verify whether account still exists is not occupied
            statement = conn.prepareStatement(
                    "SELECT * FROM account WHERE id = ?");
            statement.setInt(1, targetAccount.getId());
            result = statement.executeQuery();
            if (!result.next()) {
                // todo: error - konto neexistuje
                System.out.println("konto neexistuje");
                throw new SQLException();
            }

            // modify account
            if (targetAccount.getPassword().equals("")) {
                // no password -> do not modify password
                statement = conn.prepareStatement(
                        "UPDATE account SET name = ?, surname = ?, login = ?, admin = ? WHERE id = ?");
                statement.setInt(5, targetAccount.getId());
            } else {
                statement = conn.prepareStatement(
                        "UPDATE account SET name = ?, surname = ?, login = ?, admin = ?, password = sha2(?,256), WHERE id = ?");
                statement.setString(5, targetAccount.getPassword());
                statement.setInt(6, targetAccount.getId());
            }
            statement.setString(1, targetAccount.getName());
            statement.setString(2, targetAccount.getSurname());
            statement.setString(3, targetAccount.getLogin());
            statement.setBoolean(4, targetAccount.isAdmin());


            if (statement.executeUpdate() != 1) {
                // todo: nepodarilo sa vytvorit konto
                System.out.println("nepodarilo sa vytvorit nove konto");
                throw new SQLException();
            }

            conn.commit();

        } catch (Throwable e) {
            e.printStackTrace();
            try {
                assert conn != null;
                conn.rollback(savepoint1);
            } catch (SQLException ex) {
                Logger.getLogger(QueryHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
