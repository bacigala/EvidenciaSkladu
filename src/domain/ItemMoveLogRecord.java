package domain;

/**
 * Represents one transaction - (date, amount, username)
 */

public class ItemMoveLogRecord {
    private String date, amount, username, expiration;

    public ItemMoveLogRecord(String date, String amount, String username) {
        new ItemMoveLogRecord(date, amount, username, "združené");
    }

    public ItemMoveLogRecord(String date, String amount, String username, String expiration) {
        this.date = date;
        this.amount = amount;
        this.username = username;
        this.expiration = expiration;
    }

    public String getDate() { return date; }
    public String getAmount() { return amount; }
    public String getUsername() {return username; }
    public String getExpiration() {
        return expiration;
    }

}
