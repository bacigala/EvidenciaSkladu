package databaseAccess.CustomExceptions;

public class SQLWarningException extends Exception {
    public SQLWarningException(String message) {
        super(message);
    }
}
