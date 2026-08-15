import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/sunrise_dental";

    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection()
            throws SQLException {

        try {

            Class.forName("com.mysql.cj.jdbc.Driver");

            System.out.println(
                    "MySQL Driver loaded successfully."
            );

        } catch (ClassNotFoundException e) {

            System.out.println(
                    "MYSQL DRIVER NOT FOUND!"
            );

            e.printStackTrace();

            throw new SQLException(
                    "MySQL JDBC Driver not found.",
                    e
            );
        }

        return DriverManager.getConnection(
                URL,
                USER,
                PASSWORD
        );
    }
}