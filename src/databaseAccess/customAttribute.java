package databaseAccess;

/**
 *  Represents couple (attribute; value)
 */

public class customAttribute {
    private final String name, value;

    public customAttribute(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
