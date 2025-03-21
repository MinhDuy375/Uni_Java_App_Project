package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:resources/Uni_JavaDeskApp.db";
    private static Connection connection = null;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName("org.sqlite.JDBC"); // Load JDBC driver
                connection = DriverManager.getConnection(URL);
                System.out.println("Kết nối SQLite thành công!");
            } catch (ClassNotFoundException e) {
                System.out.println("Lỗi: Không tìm thấy driver SQLite!");
                e.printStackTrace();
            } catch (SQLException e) {
                System.out.println("Lỗi kết nối SQLite!");
                e.printStackTrace();
            }
        }
        return connection;
    }
}
