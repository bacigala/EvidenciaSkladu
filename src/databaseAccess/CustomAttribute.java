package databaseAccess;

/**
 *  Represents couple (attribute; value)
 */

public class CustomAttribute {
    private final String name, value;

    public CustomAttribute(String name, String value) {
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
