import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/sunrise_dental";

    private static final String USER =
            "root";

    private static final String PASSWORD =
            "";

    /*
     * Singleton instance
     */
    private static DBConnection instance;

    /*
     * Private constructor prevents other classes
     * from creating DBConnection objects.
     */
    private DBConnection() {

        try {

            Class.forName(
                    "com.mysql.cj.jdbc.Driver"
            );

            System.out.println(
                    "MySQL Driver loaded successfully."
            );

        } catch (ClassNotFoundException e) {

            throw new RuntimeException(
                    "MySQL JDBC Driver not found.",
                    e
            );
        }
    }

    /*
     * Singleton method
     */
    public static synchronized DBConnection getInstance() {

        if (instance == null) {

            instance =
                    new DBConnection();
        }

        return instance;
    }

    /*
     * Used by Singleton implementation
     */
    public Connection getDatabaseConnection()
            throws SQLException {

        return DriverManager.getConnection(
                URL,
                USER,
                PASSWORD
        );
    }

    /*
     * Keep this method because our previous
     * classes are already using:
     *
     * DBConnection.getConnection()
     */
    public static Connection getConnection()
            throws SQLException {

        return getInstance()
                .getDatabaseConnection();
    }
}