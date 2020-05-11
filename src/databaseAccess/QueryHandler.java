
package databaseAccess;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
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
            statement = conn.prepareStatement("SELECT * FROM account WHERE login = ? AND password = ?");
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
    
}
