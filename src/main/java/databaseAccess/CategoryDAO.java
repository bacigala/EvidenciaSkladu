package databaseAccess;

import databaseAccess.CustomExceptions.UserWarningException;
import domain.Category;

import java.sql.*;
import java.util.HashMap;

/**
 * Manages communication with DB related to Category.
 * Provides actual category list.
 * Singleton.
 */

public class CategoryDAO {
    // singleton
    private CategoryDAO() {}
    private static final CategoryDAO categoryDAO = new CategoryDAO();
    public static CategoryDAO getInstance() { return categoryDAO; }

    // lastly retrieved map of categories (Key = ID of the category)
    private HashMap<Integer, Category> categoryMap = new HashMap<>();

    /**
     * Reloads map of current categories from DB.
     */
    public void reloadCatList() throws Exception {
        if (!Login.getInstance().hasUser()) throw new UserWarningException("Prihláste sa prosím.");

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        HashMap<Integer, Category> newCategoryMap = new HashMap<>();

        try {
            conn = ConnectionFactory.getInstance().getConnection();
            statement = conn.prepareStatement(
                    "SELECT * FROM category");
            result = statement.executeQuery();
            while (result.next()) {
                Category cat = new Category(
                        result.getInt("id"),
                        result.getInt("subcat_of"),
                        result.getString("name"),
                        result.getString("color"),
                        result.getString("note")
                );
                newCategoryMap.put(cat.getId(), cat);
            }
        } finally {
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) ConnectionFactory.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        categoryMap = newCategoryMap;
    }

    /**
     * @return lastly retrieved map of categories
     */
    public HashMap<Integer,Category> getCategoryMap() {
        return categoryMap;
    }

    /**
     * Removes all cached records (e.g. on logoff).
     */
    public void dropCategoryMap() {
        categoryMap.clear();
    }

    /**
     * Tries to modify a category record.
     * @param targetCategory - category to be modified.
     */
    public void modifyCategory(Category targetCategory) throws Exception {
        if (!Login.getInstance().hasAdmin()) throw new UserWarningException("Prihláste sa prosím.");
        if (targetCategory == null) throw new NullPointerException();
        if (targetCategory.getId() == 1) throw new IllegalArgumentException(); // no change to default category

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        Savepoint savepoint1 = null;

        try {
            conn = ConnectionFactory.getInstance().getConnection();
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            savepoint1 = conn.setSavepoint("Savepoint1");

            // verify whether category still exists / is not being modified
            statement = conn.prepareStatement(
                    "SELECT * FROM category WHERE id = ?");
            statement.setInt(1, targetCategory.getId());
            result = statement.executeQuery();
            if (!result.next()) {
                throw new UserWarningException("Kategória (už) neexistuje.");
            }

            // modify the category
            statement = conn.prepareStatement("UPDATE category SET name = ?, note = ? WHERE id = ?");
            statement.setString(1, targetCategory.getName());
            statement.setString(2, targetCategory.getNote());
            statement.setInt(3, targetCategory.getId());
            if (statement.executeUpdate() != 1) throw new SQLException();

            conn.commit();

        } catch (Exception e) {
            try {
                assert conn != null;
                conn.rollback(savepoint1);
            } catch (Exception ex) {
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
     * Verifies if category has some items.
     * @param categoryId - category to be checked.
     * @return true if there are items assigned to the category.
     */
    public boolean hasItems(int categoryId) throws Exception {
        if (categoryId < 1) throw new IllegalArgumentException();
        if (!Login.getInstance().hasUser()) throw new UserWarningException("Prihláste sa prosím.");

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        boolean answer;

        try {
            conn = ConnectionFactory.getInstance().getConnection();
            statement = conn.prepareStatement(
                    "SELECT 1 FROM item WHERE category = ?");
            statement.setInt(1, categoryId);
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
     * Tries to delete the category.
     * @param categoryToDelete - category to be deleted.
     * @param categoryToTakeOver - category that takes all items from deleted one.
     */
    public void deleteCategory(Category categoryToDelete, Category categoryToTakeOver) throws Exception {
            if (!Login.getInstance().hasAdmin()) throw new UserWarningException("Prihláste sa prosím.");
        if (categoryToDelete == null) throw new IllegalArgumentException();
        if (categoryToDelete.getId() == 1) throw new IllegalArgumentException(); // default category cannot be deleted

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        Savepoint savepoint1 = null;

        try {
            conn = ConnectionFactory.getInstance().getConnection();
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            savepoint1 = conn.setSavepoint("Savepoint1");

            // check whether category to be deleted still exists
            statement = conn.prepareStatement(
                    "SELECT 1 FROM category WHERE id = ?");
            statement.setInt(1, categoryToDelete.getId());
            result = statement.executeQuery();
            if (!result.next()) throw new UserWarningException("Zvolená kategoria (už) neexistuje.");

            // check whether category to be deleted contains some items
            statement = conn.prepareStatement(
                    "SELECT 1 FROM item WHERE category = ?");
            statement.setInt(1, categoryToDelete.getId());
            result = statement.executeQuery();
            if (result.next()) {
                // category to be deleted is not empty
                if (categoryToTakeOver == null) throw new NullPointerException();

                // set new category for affected items
                statement = conn.prepareStatement(
                        "UPDATE item SET category = ? WHERE category = ?");
                statement.setInt(1, categoryToTakeOver.getId());
                statement.setInt(2, categoryToDelete.getId());
                if (statement.executeUpdate() < 1) throw new SQLException();
            }

            // delete category record
            statement = conn.prepareStatement("DELETE FROM category WHERE id = ?");
            statement.setInt(1, categoryToDelete.getId());
            if (statement.executeUpdate() != 1) throw new SQLException();

            conn.commit();

        } catch (Throwable e) {
            try {
                assert conn != null;
                conn.rollback(savepoint1);
            } catch (SQLException ex) {
                e.printStackTrace();
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
     * Tries to add a new category.
     * @param newCategory - category to be added.
     */
    public void createCategory(Category newCategory) throws Exception {
        if (!Login.getInstance().hasUser()) throw new UserWarningException("Prihláste sa prosím.");
        if (newCategory == null) throw new NullPointerException();

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        Savepoint savepoint1 = null;

        try {
            conn = ConnectionFactory.getInstance().getConnection();
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            savepoint1 = conn.setSavepoint("Savepoint1");

            // verify whether category name is not occupied
            statement = conn.prepareStatement(
                    "SELECT * FROM category WHERE name = ?");
            statement.setString(1, newCategory.getName());
            result = statement.executeQuery();
            if (result.next()) {
                throw new UserWarningException("Rovnomenná kategoória už existuje.");
            }

            // create new category
            statement = conn.prepareStatement(
                    "INSERT INTO category SET name = ?, note = ?, color = ?, subcat_of = ?");
            statement.setString(1, newCategory.getName());
            statement.setString(2, newCategory.getNote());
            statement.setString(3, newCategory.getColor());
            statement.setInt(4, newCategory.getSubCatOf());
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

}
