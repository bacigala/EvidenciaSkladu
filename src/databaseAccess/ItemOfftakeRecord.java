package databaseAccess;

import javafx.scene.control.SplitMenuButton;

import java.time.LocalDate;

/**
 * Represents one record of a triple (expiration; currentAmount, requestedAmount)
 */

public class ItemOfftakeRecord implements Comparable<ItemOfftakeRecord> {

    private final LocalDate expiration;
    private final Integer currentAmount;
    private int requestedAmount;

    public ItemOfftakeRecord(LocalDate expiration, int currentAmount) {
        this.expiration = expiration;
        this.currentAmount = currentAmount;
        this.requestedAmount = 0;
    }

    public LocalDate getExpiration() { return expiration; }

    public int getCurrentAmount() { return currentAmount; }

    public String getRequestedAmount() { return String.valueOf(requestedAmount); }

    public void setRequestedAmount(int requestedAmount) { this.requestedAmount = requestedAmount; }

    public int compareTo(ItemOfftakeRecord other) {
        return expiration.compareTo(other.getExpiration());
    }
}
