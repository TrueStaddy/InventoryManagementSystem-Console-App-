package com.inventory.management.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String CONNECTION_URL = "jdbc:sqlserver://localhost:1433;"
            + "databaseName=m_bak;"
            + "encrypt=true;"
            + "trustServerCertificate=true;"
            + "user=sa;"
            + "password=BREZOVIr1;"
            + "characterEncoding=UTF-8;"
            + "useUnicode=true;";

    static {
        try {
            // Установка кодировки для консоли
            System.setProperty("file.encoding", "UTF-8");
            System.setProperty("console.encoding", "UTF-8");
        } catch (Exception e) {
            System.err.println("Failed to set encoding: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(CONNECTION_URL);
    }
}
