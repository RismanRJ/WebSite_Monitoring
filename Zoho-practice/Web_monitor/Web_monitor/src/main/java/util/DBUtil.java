package util;

import java.sql.*;

public class DBUtil {
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found", e);
        }
        String url = "jdbc:mysql://localhost:3321/monitoring_db";
        String user = "root";
        String pass = "root";
        return DriverManager.getConnection(url, user, pass);
    }

}
