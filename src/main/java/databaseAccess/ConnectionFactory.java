package databaseAccess;

import databaseAccess.CustomExceptions.UserWarningException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Creates, manages and closes all connections to DB server.
 * Singleton. Connection pool.
 *
 * todo: connection pool is not used - current implementation uses max. 2 connections at time
 */

public class ConnectionFactory {
    // singleton
    private ConnectionFactory() {}
    private static final ConnectionFactory connectionFactory = new ConnectionFactory();
    public static ConnectionFactory getInstance() { return connectionFactory; }

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

    /**
     * @return connection from the pool or a new if the pool is empty
     */
    Connection getConnection() throws SQLException {
        Connection connection;
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
            throw new UserWarningException("Server je nedostupný.");
        }
        usedConnections.add(connection);
        connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        connection.setAutoCommit(true);

        return connection;
    }

    /**
     * Adds the connection to the pool or closes the connection if the pool is full.
     * @param connection No longer needed connection.
     */
    // todo: V aktualnej implementacii sa nepredpoklada vytvorenie viac ako 2 Connection za behu programu
    // todo: Pri rozsirovani treba zabezpecit otvaranie a zatvaranie Connection ako 'rast dynamickeho pola'
    void releaseConnection(Connection connection) {
        if (connection == null) return;
        usedConnections.remove(connection);

        // pool disabled
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // pool enabled
//        if (connectionPool.size() < MAX_POOL_SIZE) {
//            connectionPool.add(connection);
//        } else {
//            try {
//                connection.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
    }

    /**
     * @return true if valid connection details are present and connection can be established.
     */
    public boolean hasValidConnectionDetails() {
        Connection connection = null;
        boolean result;
        try {
            connection = getConnection();
            result = connection != null;
        } catch (Exception e) {
            result = false;
        } finally {
            if (connection != null) releaseConnection(connection);
        }
        return result;
    }

    /**
     * Closes all connections.
     */
    private void closeAllConnections() {
        Iterator<Connection> iterator;
        Connection connection;
        try {
            iterator = usedConnections.iterator();
            while (iterator.hasNext()) {
                connection = iterator.next();
                connection.close();
                usedConnections.remove(connection);
            }
            iterator = connectionPool.iterator();
            while (iterator.hasNext()) {
                connection = iterator.next();
                connection.close();
                connectionPool.remove(connection);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Setups connection with basic database privileges. (additional protection)
     * @return true if logged in.
     */
    public boolean setBasicUserConnectionDetails() {
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
