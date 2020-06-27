package databaseAccess.CustomExceptions;

import java.sql.SQLException;

public class ConcurrentModificationException extends SQLException {
    public ConcurrentModificationException() {
        super();
    }
}
