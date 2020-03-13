
package mainWindow;

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
    
    public String getIp() {
        return ip;
    }
    
    public String getPort() {
        return port;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }
}
