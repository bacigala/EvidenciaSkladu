package databaseAccess;

/**
 * Represents one transaction - (date, amount, username)
 */

public class ItemMoveLogRecord {
    private String date, amount, username;

    public ItemMoveLogRecord(String date, String amount, String username) {
        this.date = date;
        this.amount = amount;
        this.username = username;
    }

    public String getDate() { return date; }
    public String getAmount() { return amount; }
    public String getUsername() {return username; }

}
