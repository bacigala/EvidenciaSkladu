package databaseAccess;

import databaseAccess.CustomExceptions.UserWarningException;
import domain.Account;
import javafx.collections.ObservableList;

import java.sql.*;

/**
 * Manages communication with DB related to Account.
 * Singleton.
 */

public class AccountDAO {
    // singleton
    private AccountDAO() {}
    private static final AccountDAO accountDAO = new AccountDAO();
    public static AccountDAO getInstance() { return accountDAO; }

    /**
     * Returns current records in DB table 'account'.
     * @param accounts list to be filled with retrieved records
     */
    public void getAccounts(ObservableList<Account> accounts) throws Exception {
        if (!Login.getInstance().hasUser()) throw new UserWarningException("Prihláste sa prosím.");
        if (accounts == null) throw new  NullPointerException();

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            conn = ConnectionFactory.getInstance().getConnection();
            statement = conn.prepareStatement(
                    "SELECT id, name, surname, login, admin FROM `account` WHERE id <> 1 " +
                            "ORDER BY admin DESC, surname ASC");
            result = statement.executeQuery();
            while (result.next())
                accounts.add(new Account(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getString("surname"),
                        result.getString("login"),
                        "", // password in not retrieved from DB
                        result.getBoolean("admin")
                ));
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) ConnectionFactory.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Tries to add a new user account.
     * @param newAccount account to be added
     */
    public void createAccount(Account newAccount) throws Exception {
        if (!Login.getInstance().hasAdmin()) throw new UserWarningException("Prihláste sa prosím.");
        if (newAccount == null) throw new NullPointerException();

        // blank password not allowed
        if (newAccount.getPassword().equals("")) throw new UserWarningException("Prosím vyplňte heslo.");

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        Savepoint savepoint1 = null;

        try {
            conn = ConnectionFactory.getInstance().getConnection();
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            savepoint1 = conn.setSavepoint("Savepoint1");

            // verify whether username is not occupied
            statement = conn.prepareStatement(
                    "SELECT * FROM account WHERE login = ?");
            statement.setString(1, newAccount.getLogin());
            result = statement.executeQuery();
            if (result.next()) {
                String name = result.getString("name");
                String surname = result.getString("surname");
                throw new UserWarningException("Login je už obsadený. (" + name + " " + surname + ")");
            }

            // create new account
            statement = conn.prepareStatement(
                    "INSERT INTO account SET name = ?, surname = ?, login = ?, password = sha2(?,256), admin = ?");
            statement.setString(1, newAccount.getName());
            statement.setString(2, newAccount.getSurname());
            statement.setString(3, newAccount.getLogin());
            statement.setString(4, newAccount.getPassword());
            statement.setBoolean(5, newAccount.isAdmin());

            if (statement.executeUpdate() != 1) throw new SQLException();

            conn.commit();

        } catch (Exception e) {
            try {
                assert conn != null;
                conn.rollback(savepoint1);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            throw e;
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) ConnectionFactory.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Tries to modify a user account.
     * @param targetAccount account to be modified
     */
    public void modifyAccount(Account targetAccount) throws Exception {
        if (!Login.getInstance().hasAdmin()) throw new UserWarningException("Prihláste sa prosím.");
        if (targetAccount == null) throw new NullPointerException();
        // no change to 'trash' user
        if (targetAccount.getId() == 1) throw new UserWarningException("Toto konto nemožno upraviť.");
        // non-admin restrictions
        if (!Login.getInstance().hasAdmin()) {
            // non-admin cannot modify foreign account
            if (targetAccount.getId() != Login.getInstance().getLoggedUserId())
                throw new UserWarningException("Nemáte dostatočné oprávnenia.");
            // non-admin cannot modify own name
            if (!targetAccount.getFullName().equals(Login.getInstance().getLoggedUserFullName()))
                throw new UserWarningException("Mená vie meniť len administrátor.");
        }

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        Savepoint savepoint1 = null;

        try {
            conn = ConnectionFactory.getInstance().getConnection();
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            savepoint1 = conn.setSavepoint("Savepoint1");

            // verify whether account still exists and is not being changed
            statement = conn.prepareStatement(
                    "SELECT * FROM account WHERE id = ?");
            statement.setInt(1, targetAccount.getId());
            result = statement.executeQuery();
            if (!result.next()) {
                throw new UserWarningException("Zvolené konto (už) neexistuje.");
            }

            // modify account
            if (targetAccount.getPassword().equals("")) {
                // no password -> do not modify password (blank password is not allowed)
                statement = conn.prepareStatement(
                        "UPDATE account SET name = ?, surname = ?, login = ?, admin = ? WHERE id = ?");
                statement.setInt(5, targetAccount.getId());
            } else {
                statement = conn.prepareStatement(
                        "UPDATE account SET name = ?, surname = ?, login = ?, admin = ?, password = sha2(?,256) WHERE id = ?");
                statement.setString(5, targetAccount.getPassword());
                statement.setInt(6, targetAccount.getId());
            }
            statement.setString(1, targetAccount.getName());
            statement.setString(2, targetAccount.getSurname());
            statement.setString(3, targetAccount.getLogin());
            statement.setBoolean(4, targetAccount.isAdmin());

            if (statement.executeUpdate() != 1) throw new SQLException();

            conn.commit();

        } catch (Exception e) {
            try {
                assert conn != null;
                conn.rollback(savepoint1);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            throw e;
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) ConnectionFactory.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Verifies if transactions assigned to the accountId exist.
     * @param accountId - account to be checked.
     * @return true if there are transactions assigned to the accountId.
     */
    public boolean hasTransactions(int accountId) throws Exception {
        if (!Login.getInstance().hasUser()) throw new UserWarningException("Prihláste sa prosím.");
        if (accountId <= 0) throw new IllegalArgumentException();

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        boolean answer;

        try {
            conn = ConnectionFactory.getInstance().getConnection();
            assert conn != null;
            statement = conn.prepareStatement(
                    "SELECT 1 FROM move WHERE account_id = ?");
            statement.setInt(1, accountId);
            result = statement.executeQuery();
            answer = result.next();
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) ConnectionFactory.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return answer;
    }

    /**
     * Tries to delete the user account.
     * @param accountToDelete - account to be deleted.
     * @param accountToTakeOver - account that takes all transactions from deleted one.
     */
    public void deleteAccount(Account accountToDelete, Account accountToTakeOver) throws Exception {
        if (!Login.getInstance().hasUser()) throw new UserWarningException("Prihláste sa prosím.");
        if (!Login.getInstance().hasAdmin()) throw new UserWarningException("Nemáte dostatočné oprávnenia.");
        if (accountToDelete == null) throw new NullPointerException();
        if (accountToDelete.getId() == 1) throw new IllegalArgumentException(); // no change to 'trash' user

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        Savepoint savepoint1 = null;

        try {
            conn = ConnectionFactory.getInstance().getConnection();
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            savepoint1 = conn.setSavepoint("Savepoint1");

            // verify whether account to be deleted exists
            statement = conn.prepareStatement(
                    "SELECT 1 FROM account WHERE id = ?");
            statement.setInt(1, accountToDelete.getId());
            result = statement.executeQuery();
            if (!result.next()) throw new UserWarningException("Zvolené konto (už) neexistuje.");

            // restrict deletion of the last administrator account
            statement = conn.prepareStatement(
                    "SELECT 1 FROM account WHERE id <> ? and admin = 1");
            statement.setInt(1, accountToDelete.getId());
            result = statement.executeQuery();
            if (!result.next()) throw new UserWarningException("Nemožno odstrániť jedniného administrátora.");

            // verify whether transactions with given userId exist
            statement = conn.prepareStatement(
                    "SELECT 1 FROM move WHERE account_id = ?");
            statement.setInt(1, accountToDelete.getId());
            result = statement.executeQuery();
            if (result.next()) {
                // some transactions are associated with account to be deleted
                if (accountToTakeOver == null) throw new NullPointerException();
                // verify that account to take over exists
                statement = conn.prepareStatement(
                        "SELECT 1 FROM account WHERE id = ?");
                statement.setInt(1, accountToTakeOver.getId());
                result = statement.executeQuery();
                if (!result.next()) throw new UserWarningException("Konto na prevzatie transakcii (už) neexistuje.");

                // 'move' transactions to the other account
                statement = conn.prepareStatement(
                        "UPDATE move SET account_id = ? WHERE account_id = ?");
                statement.setInt(1, accountToTakeOver.getId());
                statement.setInt(2, accountToDelete.getId());
                if (statement.executeUpdate() < 1) throw new SQLException();
            }

            // delete account record
            statement = conn.prepareStatement("DELETE FROM account WHERE id = ?");
            statement.setInt(1, accountToDelete.getId());
            if (statement.executeUpdate() != 1) throw new SQLException();

            conn.commit();

        } catch (Throwable e) {
            e.printStackTrace();
            try {
                assert conn != null;
                conn.rollback(savepoint1);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            throw e;
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) ConnectionFactory.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
