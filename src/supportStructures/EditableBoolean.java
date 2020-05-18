package supportStructures;

public class EditableBoolean {
    private boolean value;

    public EditableBoolean(boolean value) {
        this.value = value;
    }

    public boolean get() { return value; }
    public void set(boolean value) {this.value = value; }
}
