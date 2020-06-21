package databaseAccess;

import dialog.DialogFactory;
import javafx.scene.control.Alert;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Creates, manages and closes all connections to DB server.
 * Manages user login and password verification.
 * Singleton. Connection pool.
 */

public class ConnectionFactory {
    // (default) connection details
    private String databaseIp = "192.168.0.10";
    private String databasePort = "3306";
    private String databaseName = "zubardb";
    private String databaseUsername;
    private String databasePassword;

    // connection pool
    private List<Connection> connectionPool = new ArrayList<>();
    private HashSet<Connection> usedConnections = new HashSet<>();
    private static int MAX_POOL_SIZE = 10;

    // singleton
    private ConnectionFactory() {}
    private static final ConnectionFactory connectionFactory = new ConnectionFactory();
    static ConnectionFactory getInstance() { return connectionFactory; }

    /**
     * @return connection from the pool or a new if the pool is empty
      */
    Connection getConnection() {
        Connection connection = null;
        if (!connectionPool.isEmpty()) {
            // use connection from the pool
            connection = connectionPool.remove(connectionPool.size() - 1);
            usedConnections.add(connection);
            return connection;
        }
        // create a new connection
        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + databaseIp + ":" + databasePort + "/"
                    + databaseName, databaseUsername, databasePassword);
        } catch (SQLException e) {
            e.printStackTrace();
            DialogFactory.getInstance().showAlert(Alert.AlertType.WARNING, "Server je nedostupný.");
        }
        return connection;
    }

    /**
     * Adds the connection to the pool or closes the connection if the pool is full.
     * @param connection No longer needed connection.
     */
    // todo: V aktualnej implementacii sa nepredpoklada vytvorenie viac ako 2 Connection za behu programu
    // todo: Pri rozsirovani treba zabezpecit otvaranie a zatvaranie Connection ako 'rast dynamickeho pola'
    void releaseConnection(Connection connection) {
        usedConnections.remove(connection);
        if (connectionPool.size() < MAX_POOL_SIZE) {
            connectionPool.add(connection);
        } else {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @return true if valid connection details are present and connection can be established.
     */
    boolean hasValidConnectionDetails() {
        Connection connection = getConnection();
        boolean result = connection != null;
        releaseConnection(connection);
        return result;
    }

    /**
     * Closes all connections.
     */
    private void closeAllConnections() {
        try {
            for (Connection connection : usedConnections) {
                connection.close();
                usedConnections.remove(connection);
            }
            for (Connection connection : connectionPool) {
                connection.close();
                usedConnections.remove(connection);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Setups connection with basic database privileges. (additional protection)
     * @return true if logged in.
     */
    boolean setBasicUserConnectionDetails() {
        closeAllConnections();
        databaseUsername = "basic-user";
        databasePassword = "CwJNF7zJciaxMY3v";
        return hasValidConnectionDetails();
    }

    /**
     * Setups connection with admin database privileges.
     * @return true if logged in.
     */
    boolean setAdminUserConnectionDetails() {
        closeAllConnections();
        databaseUsername = "admin-user";
        databasePassword = "scfAT4nHm5MKJu9D";
        return hasValidConnectionDetails();
    }

    /**
     * Changes connection details.
     * @param ip IP address of the server.
     * @param port port of the server.
     */
    public boolean setConnectionDetails(String ip, String port) {
        databaseIp = ip;
        databasePort = port;
        return setBasicUserConnectionDetails();
    }

    public String getDatabaseIp() {
        return databaseIp;
    }

    public String getDatabasePort() {
        return databasePort;
    }

}
