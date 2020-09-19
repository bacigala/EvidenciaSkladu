package databaseAccess;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages user login and password verification.
 * Singleton.
 */

public class Login {
    // singleton
    private Login() {}
    private static final Login login = new Login();
    public static Login getInstance() { return login; }

    // logged in user info
    private boolean loggedUserAdmin = false;
    private String loggedUserUsername = "";
    private String loggedUserFullName = "";
    private int loggedUserId = 0;

    ConnectionFactory connFactory = ConnectionFactory.getInstance();

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
            logOut();
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) connFactory.releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // save username for later pre-filled login forms
        if (answer) {
            try {
                Properties appProps = new Properties();
                Path PropertyFile = Paths.get("EvidenciaSkladu.properties");
                Reader PropReader = Files.newBufferedReader(PropertyFile);
                appProps.load(PropReader);
                appProps.setProperty("username", username);

                Writer PropWriter = Files.newBufferedWriter(PropertyFile);
                appProps.store(PropWriter, "Application Properties");
                PropWriter.close();
            } catch (Exception Ex) {
                System.out.println("write Exception: " + Ex.getMessage());
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

        //delete login-required access content
        ItemDAO.dropItemList();
        CategoryDAO.getInstance().dropCategoryMap();
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
