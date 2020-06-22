
package databaseAccess;

import domain.Account;
import domain.ConsumptionOverviewRecord;
import domain.ExpiryDateWarningRecord;
import domain.Item;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class responsible for all cumulative DB queries.
 * Singleton.
 */

public class ComplexQueryHandler {
    // singleton
    private static final ComplexQueryHandler queryHandler = new ComplexQueryHandler();
    private ComplexQueryHandler() {}
    public static ComplexQueryHandler getInstance() { return queryHandler; }


    /**
     * Return current records in DB table 'account'.
     * @param accounts - list to be filled with retrieved records.
     * @return true in success.
     */
    public boolean getAccounts(ObservableList<Account> accounts) {
        if (!ConnectionFactory.getInstance().hasValidConnectionDetails() || !Login.getInstance().hasUser()) return false;
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        Savepoint savepoint1 = null;

        try {
            conn = ConnectionFactory.getInstance().getConnection();
            assert conn != null;
            // load current records of accounts
            statement = conn.prepareStatement(
                    "SELECT id, name, surname, login, admin FROM `account` ORDER BY admin DESC, surname ASC");
            result = statement.executeQuery();
            while (result.next()) {
                Account account =  new Account(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getString("surname"),
                        result.getString("login"),
                        "", // password in not retrieved from DB.
                        result.getBoolean("admin")
                );
                accounts.add(account);
            }

        } catch (Throwable e) {
            e.printStackTrace();
            try {
                assert conn != null;
                conn.rollback(savepoint1);
            } catch (SQLException ex) {
                Logger.getLogger(ComplexQueryHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * Tries to add a new user account.
     * @param newAccount - account to be added.
     * @return true in success.
     */
    public boolean createAccount(Account newAccount) {
        if (!ConnectionFactory.getInstance().hasValidConnectionDetails() || !Login.getInstance().hasUser()) return false;

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
                System.out.println("login je obsadeny");
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
                System.out.println("nepodarilo sa vytvorit nove konto");
                throw new SQLException();
            }

            conn.commit();

        } catch (Throwable e) {
            e.printStackTrace();
            try {
                assert conn != null;
                conn.rollback(savepoint1);
            } catch (SQLException ex) {
                Logger.getLogger(ComplexQueryHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * Tries to modify a user account.
     * @param targetAccount - account to be modified.
     * @return true in success.
     */
    public boolean modifyAccount(Account targetAccount) {
        if (!ConnectionFactory.getInstance().hasValidConnectionDetails() || !Login.getInstance().hasUser()) return false;

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

            // verify whether account still exists is not occupied
            statement = conn.prepareStatement(
                    "SELECT * FROM account WHERE id = ?");
            statement.setInt(1, targetAccount.getId());
            result = statement.executeQuery();
            if (!result.next()) {
                // todo: error - konto neexistuje
                System.out.println("konto neexistuje");
                throw new SQLException();
            }

            // modify account
            if (targetAccount.getPassword().equals("")) {
                // no password -> do not modify password
                statement = conn.prepareStatement(
                        "UPDATE account SET name = ?, surname = ?, login = ?, admin = ? WHERE id = ?");
                statement.setInt(5, targetAccount.getId());
            } else {
                statement = conn.prepareStatement(
                        "UPDATE account SET name = ?, surname = ?, login = ?, admin = ?, password = sha2(?,256), WHERE id = ?");
                statement.setString(5, targetAccount.getPassword());
                statement.setInt(6, targetAccount.getId());
            }
            statement.setString(1, targetAccount.getName());
            statement.setString(2, targetAccount.getSurname());
            statement.setString(3, targetAccount.getLogin());
            statement.setBoolean(4, targetAccount.isAdmin());


            if (statement.executeUpdate() != 1) {
                // todo: nepodarilo sa vytvorit konto
                System.out.println("nepodarilo sa vytvorit nove konto");
                throw new SQLException();
            }

            conn.commit();

        } catch (Throwable e) {
            e.printStackTrace();
            try {
                assert conn != null;
                conn.rollback(savepoint1);
            } catch (SQLException ex) {
                Logger.getLogger(ComplexQueryHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) conn.close();
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
        if (!ConnectionFactory.getInstance().hasValidConnectionDetails() || !Login.getInstance().hasUser()) return false;

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        boolean answer = true;

        try {
            conn = ConnectionFactory.getInstance().getConnection();
            assert conn != null;

            // verify whether transactions with given userId exist
            statement = conn.prepareStatement(
                    "SELECT 1 FROM move WHERE account_id = ?");
            statement.setInt(1, accountId);
            result = statement.executeQuery();
            if (!result.next()) {
                // na pouzivatela nie su napisane transakcie
                answer = false;
            }

        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) conn.close();
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
        if (!ConnectionFactory.getInstance().hasValidConnectionDetails() || !Login.getInstance().hasUser()) return false;
        if (accountToDelete == null) return false;

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        Savepoint savepoint1 = null;
        boolean ans = false;

        try {
            conn = ConnectionFactory.getInstance().getConnection();
            assert conn != null;
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            savepoint1 = conn.setSavepoint("Savepoint1");

            // verify whether transactions with given userId exist
            statement = conn.prepareStatement(
                    "SELECT 1 FROM move WHERE account_id = ?");
            statement.setInt(1, accountToDelete.getId());
            result = statement.executeQuery();
            if (result.next()) {
                // na pouzivatela su napisane transakcie
                if (accountToTakeOver == null) throw new IllegalArgumentException();

                // nahradime 'majitela transakcii'
                statement = conn.prepareStatement(
                        "UPDATE move SET account_id = ? WHERE account_id = ?");
                statement.setInt(1, accountToTakeOver.getId());
                statement.setInt(2, accountToDelete.getId());
                if (statement.executeUpdate() < 1) {
                    // todo error
                    System.out.println("nepodarilo previest transakcie...");
                    throw new SQLException();
                }

            }

            // odstranime zaznam o pouzivatelovi
            statement = conn.prepareStatement("DELETE FROM account WHERE id = ?");
            statement.setInt(1, accountToDelete.getId());
            if (statement.executeUpdate() != 1) throw new SQLException();

            conn.commit();
            ans = true;

        } catch (Throwable e) {
            e.printStackTrace();
            try {
                assert conn != null;
                conn.rollback(savepoint1);
            } catch (SQLException ex) {
                Logger.getLogger(ComplexQueryHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return ans;
    }

    /**
     * Retrives all soon expiry Items from DB.
     * @param logRecords list to store retrieved data in.
     * @return true on success.
     */
    public boolean getSoonExpiryItems(ObservableList<ExpiryDateWarningRecord> logRecords) {
        if (!ConnectionFactory.getInstance().hasValidConnectionDetails() || !Login.getInstance().hasUser()) return false;

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            conn = ConnectionFactory.getInstance().getConnection();
            assert conn != null;
            statement = conn.prepareStatement(
                    "SELECT item.id, item.name, SUM(move_item.amount) AS expiry_amount, move_item.expiration " +
                            "FROM (move_item JOIN item ON (move_item.item_id = item.id)) " +
                            "WHERE move_item.expiration < NOW() - 10 " +
                            "GROUP BY move_item.expiration, item.id " +
                            "HAVING expiry_amount > 0 " +
                            "ORDER BY item.name ASC, move_item.expiration ASC");
            result = statement.executeQuery();
            while (result.next()) {
                ExpiryDateWarningRecord logRecord =  new ExpiryDateWarningRecord(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getDate("expiration"),
                        result.getInt("expiry_amount")
                );
                logRecords.add(logRecord);
            }
        } catch (SQLException e) {
            return false;
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                logRecords = null;
            }
        }
        return true;
    }

    /**
     * Retrives all low on stock items.
     * @param items list to store retrieved data in.
     * @return true on success.
     */
    public boolean getLowStockItems(ObservableList<Item> items) {
        if (!ConnectionFactory.getInstance().hasValidConnectionDetails() || !Login.getInstance().hasUser()) return false;

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            conn = ConnectionFactory.getInstance().getConnection();
            assert conn != null;

            statement = conn.prepareStatement(
                    "SELECT * FROM item WHERE item.cur_amount <= item.min_amount");
            result = statement.executeQuery();
            while (result.next()) {
                Item item = new Item(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getString("barcode"),
                        result.getInt("min_amount"),
                        result.getInt("cur_amount"),
                        result.getString("unit"),
                        result.getString("note"),
                        result.getInt("category")
                );
                items.add(item);
            }
        } catch (SQLException e) {
            return false;
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                items = null;
            }
        }
        return true;
    }

    /**
     * Retrieves average consumption and trash of all items.
     * @param items list to store retrieved data in.
     * @return true on success.
     */
    public boolean getConsumptionOverviewRecords(ObservableList<ConsumptionOverviewRecord> items) {
        if (!ConnectionFactory.getInstance().hasValidConnectionDetails() || !Login.getInstance().hasUser()) return false;

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            conn = ConnectionFactory.getInstance().getConnection();
            assert conn != null;

            statement = conn.prepareStatement(
                    "WITH month_use AS (\n" +
                            "    SELECT item.id AS ids, SUM(move_item.amount) AS month_amount, CEIL(ABS(DATEDIFF(NOW(), move.time))/30) AS months_back\n" +
                            "\tFROM move_item JOIN move ON move_item.move_id = move.id JOIN item ON (move_item.item_id = item.id)\n" +
                            "\tWHERE move_item.amount < 0 AND move.account_id <> 1\n" +
                            "\tGROUP BY move_item.item_id, months_back\n" +
                            "), \n" +
                            "month_supply AS (\n" +
                            "    SELECT item.id AS id, SUM(move_item.amount) AS month_amount, CEIL(ABS(DATEDIFF(NOW(), move.time))/30) AS months_back\n" +
                            "\tFROM move_item JOIN move ON move_item.move_id = move.id JOIN item ON (move_item.item_id = item.id)\n" +
                            "\tWHERE move_item.amount > 0\n" +
                            "\tGROUP BY move_item.item_id, months_back\n" +
                            "), \n" +
                            "month_trash AS (\n" +
                            "    SELECT item.id AS id, SUM(move_item.amount) AS month_amount, CEIL(ABS(DATEDIFF(NOW(), move.time))/30) AS months_back\n" +
                            "\tFROM move_item JOIN move ON move_item.move_id = move.id JOIN item ON (move_item.item_id = item.id)\n" +
                            "\tWHERE move.account_id = 1\n" +
                            "\tGROUP BY move_item.item_id, months_back\n" +
                            "),\n" +
                            "last_month_use AS (\n" +
                            "\tSELECT * FROM month_use WHERE months_back = 0\n" +
                            "),\n" +
                            "months_in_use AS (\n" +
                            "\tSELECT month_supply.id, MAX(months_back) AS num\n" +
                            "    FROM month_supply\n" +
                            "    WHERE 1\n" +
                            "    GROUP BY id\n" +
                            "),\n" +
                            "avg_month_use AS (\n" +
                            "\tSELECT ids, SUM(month_amount) / months_in_use.num AS avgmuse\n" +
                            "    FROM month_use JOIN months_in_use ON ids = months_in_use.id\n" +
                            "    WHERE 1\n" +
                            "    GROUP BY month_use.ids    \n" +
                            "),\n" +
                            "avg_month_trash AS (\n" +
                            "\tSELECT month_trash.id, SUM(month_amount) / months_in_use.num AS avgmtrash\n" +
                            "    FROM month_trash JOIN months_in_use ON month_trash.id = months_in_use.id\n" +
                            "    WHERE 1\n" +
                            "    GROUP BY month_trash.id\n" +
                            ")\n" +
                            "SELECT item.id, item.name, ABS(last_month_use.month_amount) as last_month, ABS(avg_month_use.avgmuse) as avg_month, ABS(avg_month_trash.avgmtrash) AS avg_trash\n" +
                            "FROM (((item LEFT OUTER JOIN last_month_use ON item.id = last_month_use.ids) LEFT OUTER JOIN avg_month_use ON avg_month_use.ids = item.id) LEFT OUTER JOIN avg_month_trash ON avg_month_trash.id = item.id)\n" +
                            "WHERE 1\n"
            );
            result = statement.executeQuery();
            while (result.next()) {
                ConsumptionOverviewRecord record = new ConsumptionOverviewRecord(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getDouble("last_month"),
                        result.getDouble("avg_month"),
                        result.getDouble("avg_trash")
                );
                items.add(record);
            }
        } catch (SQLException e) {
            return false;
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                items = null;
            }
        }
        return true;
    }
}
