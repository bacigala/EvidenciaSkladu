
package databaseAccess;

/**
 * Represents one record in the table "item".
 */

public class Item {
    private int id;
    private String name;
    private String barcode;
    private int minAmount;
    private int curAmount;
    private String unit;
    private String note;
    private int category;

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
        QueryHandler qh = QueryHandler.getInstance();
        return qh.getCategoryMap().get(getCategory()).getName();
    }
}
