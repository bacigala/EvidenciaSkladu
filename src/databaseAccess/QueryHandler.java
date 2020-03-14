
package databaseAccess;

/**
 * Singleton class, performs all interaction with the database.
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QueryHandler {
    private static final QueryHandler queryHandler = new QueryHandler();
    private ConnectionDetails connDetails = null;
    private String loggedUserName = "";
    private int loggedUserId = 0;
    private ArrayList<Item> itemList = new ArrayList<>();
    private HashMap<Integer,Category> categoryMap = new HashMap<>();

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
     * Creates connection based on current connection details.
     */
    private Connection getConnection(ConnectionDetails cd) throws SQLException {
        if (cd == null) return null;               
        return DriverManager.getConnection("jdbc:mysql://" 
                + cd.getIp() + ":" + cd.getPort() + "/" 
                + cd.getDbName(), cd.getUsername(), cd.getPassword());
    }

    private Connection getConnection() throws SQLException {
        return getConnection(connDetails);
    }
        
    // sets new connection details, returns true if they are valid
    public boolean setConnectionDetails(ConnectionDetails connectionDetails) {
        if (connectionDetails == null) return false;
        Connection conn = null;
        try {
            conn = getConnection(connectionDetails);            
        } catch (SQLException e) {
            return false;
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                return false;
            }
        }
        connDetails = connectionDetails;
        return true;
    }
    
    // returns true if valid connection details are present
    public boolean hasConnectionDetails() {
        return connDetails != null;
    }
    
    // removes record about connection details
    public boolean dropConnectionDetails() {
        connDetails = null;
        return true;
    }
     
    // verifies the username and password, returns true if valid
    public boolean logIn(String username, String password) {
        if (loggedUserId > 0) return false;       
        
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;       
        
        try {   
            conn = getConnection();
            statement = conn.prepareStatement(
                    "SELECT * FROM account WHERE login = ? AND password = ?");
            statement.setString(1, username);
            statement.setString(2, password);
            result = statement.executeQuery();
            if (result.next()) {
                loggedUserName = result.getString("name") + " "
                        + result.getString("surname");
                loggedUserId = result.getInt("id");
            } else {
                return false;
            }            
        } catch (SQLException e) {
            return false;
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                
            }
        }
        return true;
    }
    
    // removes saved info about logged in user
    public void logOut() {
        loggedUserId = 0;
        loggedUserName = "";
    }
    
    // returns logged in user ID
    public int getLoggedUserId() {
        return loggedUserId;
    }

    // returns full name of currently logged user
    public String getLoggedUserName() {
        return loggedUserName;
    }
    
    // returns rurrently set connection details
    public ConnectionDetails getConnectionDettails() {
        return connDetails;
    }
    
    // returns true if verified user is logged in
    public boolean hasUser() {
        return loggedUserId > 0;
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
    
    // ITEM SUPPLY - supplies specified amount of items in name of currently logged user to database, returns true on success
    public boolean itemSupply(int itemId, int supplyAmount, LocalDate expiration) {
        if (!hasConnectionDetails() || !hasUser()) return false;
                 
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null; 
        Savepoint savepoint1 = null;
        ArrayList<Item> newItemList = new ArrayList<>();
        int curAmount;
        int moveId;
        
        try {   
            conn = getConnection(); 
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
            
            // increase no. of items present
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
                
            }
        }
        
        return true;       
    }
    
}
