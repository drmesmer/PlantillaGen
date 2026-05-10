package com.plantillagen.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static final String PROPERTIES_FILE = "/db.properties";
    private static String url;
    private static String user;
    private static String password;

    static {
        try (InputStream is = DatabaseConnection.class.getResourceAsStream(PROPERTIES_FILE)) {
            if (is == null) {
                throw new RuntimeException("No se encontró " + PROPERTIES_FILE);
            }
            Properties props = new Properties();
            props.load(is);

            String host = props.getProperty("db.host", "localhost");
            String port = props.getProperty("db.port", "5432");
            String name = props.getProperty("db.name", "plantillagen");
            user = props.getProperty("db.user", "postgres");
            password = props.getProperty("db.password", "1234");

            url = "jdbc:postgresql://" + host + ":" + port + "/" + name;
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar db.properties", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public static void close(AutoCloseable... resources) {
        for (AutoCloseable r : resources) {
            if (r != null) {
                try {
                    r.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
