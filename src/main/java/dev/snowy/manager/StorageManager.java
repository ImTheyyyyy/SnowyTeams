package dev.snowy.manager;

import dev.snowy.SnowyTeams;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class StorageManager {
    private final String storageType;
    private Connection connection;

    public StorageManager(FileConfiguration config) {
        this.storageType = config.getString("Storage.type").toUpperCase();
    }

    public void initializeStorage() {
        if ("YAML".equals(storageType)) {
            System.out.println("Using YAML storage.");
        } else if ("MYSQL".equals(storageType)) {
            System.out.println("Using MySQL storage.");
            initializeMySQLStorage();
        } else {
            throw new IllegalArgumentException("Unsupported storage type: " + storageType);
        }
    }

    private void initializeMySQLStorage() {
        FileConfiguration config = SnowyTeams.getInstance().getConfigManager().getConfig();
        String host = config.getString("Storage.database.host");
        int port = config.getInt("Storage.database.port");
        String database = config.getString("Storage.database.database_name");
        String user = config.getString("Storage.database.user");
        String password = config.getString("Storage.database.password");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database;

        try {
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to MySQL database successfully.");

            try (Statement statement = connection.createStatement()) {
                statement.executeQuery("SELECT 1");
                System.out.println("MySQL connection is valid.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to connect to MySQL database.");
            System.out.println("Error message: " + e.getMessage());
            System.out.println("SQL state: " + e.getSQLState());
            System.out.println("Vendor error code: " + e.getErrorCode());
        }
    }

    public Connection getDatabaseConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                reconnectIfNeeded();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (connection == null) {
            System.out.println("Connection is null, make sure to initialize it before usage.");
        }
        return connection;
    }

    public void reconnectIfNeeded() throws SQLException {
        if (connection == null || connection.isClosed()) {

            FileConfiguration config = SnowyTeams.getInstance().getConfigManager().getConfig();
            String host = config.getString("Storage.database.host");
            int port = config.getInt("Storage.database.port");
            String database = config.getString("Storage.database.database_name");
            String user = config.getString("Storage.database.user");
            String password = config.getString("Storage.database.password");

            String url = "jdbc:mysql://" + host + ":" + port + "/" + database;

            connection = DriverManager.getConnection(url, user, password);
        }
    }


    public String getStorageType() {
        return storageType;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("MySQL connection closed.");
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Failed to close MySQL connection.");
            }
        }
    }
}