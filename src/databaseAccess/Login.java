package databaseAccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Manages user login and password verification.
 * Singleton.
 */

public class Login {
    ConnectionFactory connFactory = ConnectionFactory.getInstance();

    // logged in user info
    private boolean loggedUserAdmin = false;
    private String loggedUserUsername = "";
    private String loggedUserName = "";
    private int loggedUserId = 0;

    // singleton
    private Login() {}
    private static final Login login = new Login();
    public static Login getInstance() { return login; }

    /**
     * Verifies username and password.
     * @param username Username to be tested.
     * @param password Password to be tested.
     * @return true if username and password are valid.
     */
    public boolean logIn(String username, String password) {
        if (loggedUserId > 0) logOut();

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            conn = connFactory.getConnection();
            assert conn != null;
            statement = conn.prepareStatement("SELECT * FROM account WHERE login=? AND password=sha2(?,256)");
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
                connFactory.releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // if logged in user is an admin, switch to ADMIN database access
        return loggedUserAdmin ?
                connFactory.setAdminUserConnectionDetails() : connFactory.setBasicUserConnectionDetails();
    }

    /**
     * Removes information about currently logged user + sets BASIC database user for further database access.
     */
    public void logOut() {
        connFactory.setBasicUserConnectionDetails();
        loggedUserAdmin = false;
        loggedUserUsername = "";
        loggedUserName = "";
        loggedUserId = 0;

//        todo: delete login-required access content
//        itemList.clear();
//        categoryMap.clear();
    }


    public int getLoggedUserId() {
        return loggedUserId;
    }

    public String getLoggedUserName() {
        return loggedUserName;
    }

    public String getLoggedUserUsername() {
        return loggedUserUsername;
    }

    public boolean hasUser() {
        return loggedUserId > 0;
    }

    public boolean hasAdmin() {
        return hasUser() && loggedUserAdmin;
    }

}
