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
    private String loggedUserFullName = "";
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
        boolean answer = true;

        try {
            conn = connFactory.getConnection();
            assert conn != null;
            statement = conn.prepareStatement("SELECT * FROM account WHERE login=? AND password=sha2(?,256)");
            statement.setString(1, username);
            statement.setString(2, password);
            result = statement.executeQuery();
            if (result.next()) {
                loggedUserFullName = result.getString("name") + " "
                        + result.getString("surname");
                loggedUserId = result.getInt("id");
                loggedUserAdmin = result.getBoolean("admin");
                loggedUserUsername = username;
            } else {
                answer = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            answer = false;
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
        if (answer) return loggedUserAdmin ?
                connFactory.setAdminUserConnectionDetails() : connFactory.setBasicUserConnectionDetails();
        return false;
    }

    /**
     * Removes information about currently logged user + sets BASIC database user for further database access.
     */
    public void logOut() {
        connFactory.setBasicUserConnectionDetails();
        loggedUserAdmin = false;
        loggedUserUsername = "";
        loggedUserFullName = "";
        loggedUserId = 0;

//        todo: delete login-required access content
        ItemDAO.dropItemList();
//        categoryMap.clear();
    }


    public int getLoggedUserId() {
        return loggedUserId;
    }

    public String getLoggedUserFullName() {
        return loggedUserFullName;
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
