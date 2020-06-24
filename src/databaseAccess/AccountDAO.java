package databaseAccess;

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
     * @param accounts  list to be filled with retrieved records
     * @return          true on success.
     */
    public boolean getAccounts(ObservableList<Account> accounts) {
        if (!Login.getInstance().hasUser()) return false;
        if (accounts == null) return false;

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            conn = ConnectionFactory.getInstance().getConnection();
            assert conn != null;
            statement = conn.prepareStatement(
                    "SELECT id, name, surname, login, admin FROM `account` ORDER BY admin DESC, surname ASC");
            result = statement.executeQuery();
            while (result.next()) {
                accounts.add(new Account(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getString("surname"),
                        result.getString("login"),
                        "", // password in not retrieved from DB
                        result.getBoolean("admin")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) ConnectionFactory.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * Tries to add a new user account.
     * @param newAccount account to be added
     * @return           true on success
     */
    public boolean createAccount(Account newAccount) {
        if (!Login.getInstance().hasAdmin()) return false;
        if (newAccount == null) return false;

        // blank password not allowed
        if (newAccount.getPassword().equals("")) return false;

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        Savepoint savepoint1 = null;

        try {
            conn = ConnectionFactory.getInstance().getConnection();
            assert conn != null;
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            savepoint1 = conn.setSavepoint("Savepoint1");

            // verify whether username is not occupied
            statement = conn.prepareStatement(
                    "SELECT * FROM account WHERE login = ?");
            statement.setString(1, newAccount.getLogin());
            result = statement.executeQuery();
            if (result.next()) {
                // todo: login je obsadeny
                throw new SQLException();
            }

            // create new account
            statement = conn.prepareStatement(
                    "INSERT INTO account SET name = ?, surname = ?, login = ?, password = sha2(?,256), admin = ?");
            statement.setString(1, newAccount.getName());
            statement.setString(2, newAccount.getSurname());
            statement.setString(3, newAccount.getLogin());
            statement.setString(4, newAccount.getPassword());
            statement.setBoolean(5, newAccount.isAdmin());

            if (statement.executeUpdate() != 1) {
                // todo: nepodarilo sa vytvorit konto
                throw new SQLException();
            }

            conn.commit();

        } catch (Exception e) {
            e.printStackTrace();
            try {
                assert conn != null;
                conn.rollback(savepoint1);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) ConnectionFactory.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * Tries to modify a user account.
     * @param targetAccount account to be modified
     * @return true on success
     */
    public boolean modifyAccount(Account targetAccount) {
        if (!Login.getInstance().hasUser()) return false;
        if (targetAccount == null) return false;
        if (targetAccount.getId() == 1) return false; // no change to 'trash' user
        // non-admin restrictions
        if (!Login.getInstance().hasAdmin()) {
            // non-admin cannot modify foreign account
            if (targetAccount.getId() != Login.getInstance().getLoggedUserId()) return false;
            // non-admin cannot modify own name
            if (!targetAccount.getFullName().equals(Login.getInstance().getLoggedUserFullName())) return false;
        }

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        Savepoint savepoint1 = null;

        try {
            conn = ConnectionFactory.getInstance().getConnection();
            assert conn != null;
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            savepoint1 = conn.setSavepoint("Savepoint1");

            // verify whether account still exists and is not being changed
            statement = conn.prepareStatement(
                    "SELECT * FROM account WHERE id = ?");
            statement.setInt(1, targetAccount.getId());
            result = statement.executeQuery();
            if (!result.next()) {
                // todo: error - konto neexistuje
                throw new SQLException();
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

            if (statement.executeUpdate() != 1) {
                throw new SQLException();
            }

            conn.commit();

        } catch (Exception e) {
            e.printStackTrace();
            try {
                assert conn != null;
                conn.rollback(savepoint1);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) ConnectionFactory.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * Verifies if transactions assigned to the accountId exist.
     * @param accountId - account to be checked.
     * @return true if there are transactions assigned to the accountId.
     */
    public boolean hasTransactions(int accountId) {
        if (!Login.getInstance().hasUser()) return false;

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
        } catch (Exception e) {
            e.printStackTrace();
            return true;
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
     * @return true on success.
     */
    public boolean deleteAccount(Account accountToDelete, Account accountToTakeOver) {
        if (!Login.getInstance().hasAdmin()) return false;
        if (accountToDelete == null) return false;
        if (accountToDelete.getId() == 1) return false; // no change to 'trash' user

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        Savepoint savepoint1 = null;

        try {
            conn = ConnectionFactory.getInstance().getConnection();
            assert conn != null;
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            savepoint1 = conn.setSavepoint("Savepoint1");

            // verify whether account to be deleted exists
            statement = conn.prepareStatement(
                    "SELECT 1 FROM account WHERE id = ?");
            statement.setInt(1, accountToDelete.getId());
            result = statement.executeQuery();
            if (!result.next()) throw new IllegalArgumentException();

            // verify whether transactions with given userId exist
            statement = conn.prepareStatement(
                    "SELECT 1 FROM move WHERE account_id = ?");
            statement.setInt(1, accountToDelete.getId());
            result = statement.executeQuery();
            if (result.next()) {
                // som transactions are associated with the account
                if (accountToTakeOver == null) throw new IllegalArgumentException();
                // verify that account to take over the transactions exists
                statement = conn.prepareStatement(
                        "SELECT 1 FROM account WHERE id = ?");
                statement.setInt(1, accountToTakeOver.getId());
                result = statement.executeQuery();
                if (!result.next()) throw new IllegalArgumentException();

                // 'move' transactions to the other account
                statement = conn.prepareStatement(
                        "UPDATE move SET account_id = ? WHERE account_id = ?");
                statement.setInt(1, accountToTakeOver.getId());
                statement.setInt(2, accountToDelete.getId());
                if (statement.executeUpdate() < 1) {
                    // todo error
                    throw new SQLException();
                }
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
            return false;
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) ConnectionFactory.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

}
