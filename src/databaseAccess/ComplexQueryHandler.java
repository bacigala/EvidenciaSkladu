
package databaseAccess;

import domain.ConsumptionOverviewRecord;
import domain.ExpiryDateWarningRecord;
import domain.Item;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
     * Retrieves all soon expiry Items from DB.
     * @param logRecords list to store retrieved data in.
     * @return true on success.
     */
    public boolean getSoonExpiryItems(ObservableList<ExpiryDateWarningRecord> logRecords) {
        if (!Login.getInstance().hasUser()) return false;

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
                if (conn != null) ConnectionFactory.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * Retrieves all low on stock items.
     * @param items list to store retrieved data in.
     * @return true on success.
     */
    public boolean getLowStockItems(ObservableList<Item> items) {
        if (!Login.getInstance().hasUser()) return false;

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
                items.add(new Item(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getString("barcode"),
                        result.getInt("min_amount"),
                        result.getInt("cur_amount"),
                        result.getString("unit"),
                        result.getString("note"),
                        result.getInt("category")
                ));
            }
        } catch (SQLException e) {
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
     * Retrieves average consumption and trash of all items.
     * @param records list to store retrieved data in.
     * @return true on success.
     */
    public boolean getConsumptionOverviewRecords(ObservableList<ConsumptionOverviewRecord> records) {
        if (!Login.getInstance().hasUser()) return false;
        if (records == null) return false;

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
                            "\tSELECT * FROM month_use WHERE months_back = 1\n" +
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
                records.add(new ConsumptionOverviewRecord(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getDouble("last_month"),
                        result.getDouble("avg_month"),
                        result.getDouble("avg_trash")
                ));
            }
        } catch (SQLException e) {
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
