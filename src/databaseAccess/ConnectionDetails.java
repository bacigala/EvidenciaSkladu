
package databaseAccess;

/**
 * Represents connection details.
 */

public class ConnectionDetails {
    private final String ip;
    private final String port;
    private final String dbName;
    private final String username;
    private final String password;
    
    public ConnectionDetails(String ip, String port, String dbName,
            String username, String password) {
        this.ip = ip;
        this.port = port;
        this.dbName = dbName;
        this.username = username;
        this.password = password;
    }

    /**
     * @return IP of the connection.
     */
    public String getIp() {
        return ip;
    }

    /**
     * @return port of the connection.
     */
    public String getPort() {
        return port;
    }

    /**
     * @return name of the database.
     */
    public String getDbName() {return dbName; }

    /**
     * @return username of database user.
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return password of database user.
     */
    public String getPassword() {
        return password;
    }
}
