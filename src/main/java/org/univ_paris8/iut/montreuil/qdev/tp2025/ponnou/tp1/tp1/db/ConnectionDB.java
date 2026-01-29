package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionDB {

    private static Connection connect;

    private static final String DB_PATH = System.getProperty("user.home") + "/prive/MasterAnnonce.sqlite";
    private static final String URL = "jdbc:sqlite:" + DB_PATH;

    public static Connection getInstance() throws SQLException {
        if (connect == null || connect.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                throw new SQLException("sqlite-jdbc manquant dans WEB-INF/lib", e);
            }

            connect = DriverManager.getConnection(URL);
            initSchema(connect);
        }
        return connect;
    }

    private static void initSchema(Connection c) throws SQLException {
        String sql =
                "CREATE TABLE IF NOT EXISTS annonce (" +
                        "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "  title TEXT NOT NULL," +
                        "  description TEXT NOT NULL," +
                        "  adress TEXT NOT NULL," +
                        "  mail TEXT NOT NULL," +
                        "  date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                        ");";

        try (Statement st = c.createStatement()) {
            st.execute(sql);
        }
    }

}
