package databaseAccess.CustomExceptions;

import java.sql.SQLException;

public class UserWarningException extends SQLException {
    public UserWarningException(String message) {
        super(message);
    }
}
