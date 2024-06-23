import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class TodoApp {
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private String tableName;
    private String customText;
    private static String loginPassword;
    private final String configFilePath = "C:\\Users\\gszmo\\IdeaProjects\\bd\\s12333ss\\src\\settings.conf";

    public TodoApp() {
        loadDatabaseSettings();
    }

    private void loadDatabaseSettings() {
        Properties properties = new Properties();

        try (FileInputStream input = new FileInputStream(configFilePath)) {
            properties.load(input);
            dbUrl = properties.getProperty("adress");
            dbUser = properties.getProperty("username");
            dbPassword = properties.getProperty("password");
            tableName = properties.getProperty("tableName");
            customText = properties.getProperty("customText", "");
            loginPassword = properties.getProperty("loginPassword");
            System.out.println("Database settings loaded from " + configFilePath);
        } catch (IOException e) {
            System.err.println("Error loading settings from " + configFilePath);
            e.printStackTrace();
        }
    }

    public static String getPassword() {
        return loginPassword;
    }

    public String getTableName() {
        return tableName;
    }

    public String getCustomText() {
        return customText;
    }

    public void setCustomText(String customText) {
        this.customText = customText;
        saveDatabaseSettings();
    }

    private void saveDatabaseSettings() {
        Properties properties = new Properties();

        try (FileInputStream input = new FileInputStream(configFilePath)) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

        properties.setProperty("tableName", tableName);
        properties.setProperty("customText", customText);
        properties.setProperty("loginPassword", loginPassword);

        try (OutputStream output = new FileOutputStream(configFilePath)) {
            properties.store(output, null);
            System.out.println("Database settings saved to " + configFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<TodoItem> fetchTodosFromDatabase() {
        List<TodoItem> todoList = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            String query = "SELECT id, description, completed, box FROM " + tableName;
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String description = resultSet.getString("description");
                boolean completed = resultSet.getBoolean("completed");
                String box = resultSet.getString("box");
                todoList.add(new TodoItem(id, description, completed, box));
            }

            System.out.println("Fetched " + todoList.size() + " items from the database.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return todoList;
    }

    public void deleteTodoFromDatabase(int id) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            String query = "DELETE FROM " + tableName + " WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
            System.out.println("Deleted item with ID: " + id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteAllTodosFromDatabase(List<TodoItem> todoList) {
        for (TodoItem item : todoList) {
            deleteTodoFromDatabase(item.getId());
        }
        System.out.println("Deleted all items.");
    }

    public void markTodoAsCompletedInDatabase(int id, boolean completed) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            String query = "UPDATE " + tableName + " SET completed = ? WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setBoolean(1, completed);
            preparedStatement.setInt(2, id);
            preparedStatement.executeUpdate();
            System.out.println("Updated completion status for item with ID: " + id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateTodoBoxInDatabase(int id, String box) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            String query = "UPDATE " + tableName + " SET box = ? WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, box);
            preparedStatement.setInt(2, id);
            preparedStatement.executeUpdate();
            System.out.println("Updated box for item with ID: " + id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addTodoToDatabase(String description) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            int newId = getMaxId() + 1;
            String query = "INSERT INTO " + tableName + " (id, description, completed, box) VALUES (?, ?, FALSE, '')";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, newId);
            preparedStatement.setString(2, description);
            preparedStatement.executeUpdate();
            System.out.println("Added new todo with description: " + description);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getMaxId() {
        int maxId = 0;
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            String query = "SELECT MAX(id) AS max_id FROM " + tableName;
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            if (resultSet.next()) {
                maxId = resultSet.getInt("max_id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return maxId;
    }
}
