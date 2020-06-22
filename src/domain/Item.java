
package domain;

import databaseAccess.CategoryDAO;
import databaseAccess.ComplexQueryHandler;

/**
 * Represents one record in the table "item".
 */

public class Item {
    private final int id;
    private final String name;
    private final String barcode;
    private final int minAmount;
    private final int curAmount;
    private final String unit;
    private final String note;
    private final int category;

    public Item(int id, String name, String barcode, int minAmount,
            int curAmount, String unit, String note, int category) {
        this.id = id;
        this.name = name;
        this.barcode = barcode;
        this.minAmount = minAmount;
        this.curAmount = curAmount;
        this.unit = unit;
        this.note = note;
        this.category = category;
    }
    
    public int getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getBarcode() {
        return barcode;
    }
    
    public int getMinAmount() {
        return minAmount;
    }
    
    public int getCurAmount() {
        return curAmount;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public String getNote() {
        return note;
    }
    
    public int getCategory() {
        return category;
    }
    
    public String getCategoryName() {
        return CategoryDAO.getInstance().getCategoryMap().get(getCategory()).getName();
    }

}
