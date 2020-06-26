package domain;

import java.util.Date;

/**
 * Represents expiry warning record.
 */

public class ExpiryDateWarningRecord extends Item {
    private int expiryAmount;

    public ExpiryDateWarningRecord(int id, String name, int expiryAmount) {
        super(id, name, "", 0,0, "", "", 1);
        this.expiryAmount = expiryAmount;
    }

    public int getExpiryAmount() {return expiryAmount; }
    public void setExpiryAmount(int expiryAmount) {this.expiryAmount = expiryAmount; }

}
