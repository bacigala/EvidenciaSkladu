package databaseAccess;

/**
 * Represents one record in DB table 'Account'.
 */

public class Account {
    private int id;
    private String name, surname, login, password;
    private boolean admin;

    public Account(int id, String name, String surname, String login, String password, boolean admin) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.login = login;
        this.password = password;
        this.admin = admin;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getFullName() {
        return name + " " + surname;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public boolean isAdmin() {
        return admin;
    }

    public String getIsAdminText() {
        return admin ? "áno" : "nie";
    }
}
