package domain;

import java.util.Date;

/**
 * Represents expiry warning record.
 */

public class ExpiryDateWarningRecord extends Item {
    private Date expiryDate;
    private int expiryAmount;

    public ExpiryDateWarningRecord(int id, String name, Date expiryDate, int expiryAmount) {
        super(id, name, "", 0,0, "", "", 1);
        this.expiryDate = expiryDate;
        this.expiryAmount = expiryAmount;
    }

    public Date getExpiryDate() {return expiryDate; }
    public void setExpiryDate(Date expiryDate) {this.expiryDate = expiryDate; }
    public int getExpiryAmount() {return expiryAmount; }
    public void setExpiryAmount(int expiryAmount) {this.expiryAmount = expiryAmount; }

}
