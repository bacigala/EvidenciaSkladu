
package databaseAccess;

/**
 * Represents one record in the table "category".
 */

import java.awt.Color;

public class Category {
    private int id;
    private int subCatOf;
    private String name;
    private Color colorJava;
    private String color;
    private String note;
    
    public Category(int id, int subCatOf, String name, String color, String note) {
        this.id = id;
        this.subCatOf = subCatOf;
        this.name = name;
        this.colorJava = Color.getColor(color);
        this.color = color;
        this.note = note;
    }
    
    public int getId() {
        return id;
    }
    
    public int getSubCatOf() {
        return subCatOf;
    }
    
    public String getName() {
        return name;
    }
    
    public Color getColorJava() {
        return colorJava;
    }
    
    public String getColor() {
        return color;
    }
    
    public String getNote() {
        return note;
    }
}
