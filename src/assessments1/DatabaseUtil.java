package assessments1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/EmployeeDB", "root", "Dsaq@123");
        } catch (ClassNotFoundException | SQLException e) {
            throw new SQLException("Error connecting to the database", e);
        }
    }
}



// Final cannot be reassigned after inyiallization- cannot override - provides immutability