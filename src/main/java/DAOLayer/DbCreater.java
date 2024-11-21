package DAOLayer;

import View.ViewServlet;

import javax.servlet.ServletContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DbCreater {
    private static DbCreater instance;
    private static boolean DBisExist = false;

//    private static Properties props;
    private ViewServlet viewServlet;


    private DbCreater(ViewServlet viewServlet) {
        this.viewServlet = viewServlet;
    }
    public static DbCreater getInstance(ViewServlet viewServlet) {
        if (instance == null) {
            instance = new DbCreater(viewServlet);
        }
        return instance;
    }

    //------------------------------------------------------------------------------------------------------------------
    public void createDB(){
        if (DBisExist) {
            return;
        }
        Properties props = new Properties();
        ServletContext servletContext = viewServlet.getServletContext();
        String realPath = servletContext.getRealPath("/WEB-INF/props/database.properties");
        try (FileInputStream fis = new FileInputStream(realPath)) {
            props.load(fis);
        } catch (IOException e) {
            System.out.println("Ошибка при загрузке свойств: " + e.getMessage());
            return;
        }

        String dbName = props.getProperty("db.name");
        String dbUrl = props.getProperty("db.url");
        String dbUser = props.getProperty("db.user");
        String dbPassword = props.getProperty("db.password");
        String tableName = props.getProperty("table.name");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Connection conn = null;
        Statement stmt = null;

        try {
            // Open a connection
            conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            stmt = conn.createStatement();

            stmt.executeUpdate("SET NAMES utf8");
            stmt.executeUpdate("SET CHARACTER SET utf8");

            String sql = "CREATE DATABASE " + dbName + " CHARACTER SET utf8 COLLATE utf8_general_ci";
            stmt.executeUpdate(sql);
            System.out.println("Database created successfully...");

            // Подключение к базе данных
            String url = dbUrl + dbName;
            conn.close();
            conn = DriverManager.getConnection(url, dbUser, dbPassword);
            stmt = conn.createStatement();

            stmt.executeUpdate("SET NAMES utf8");
            stmt.executeUpdate("SET CHARACTER SET utf8");
            stmt.executeUpdate("SET COLLATION_CONNECTION=utf8_general_ci");

            // Создание таблицы
            String createTableQuery = "CREATE TABLE " + tableName + " (" +
                    "id BIGINT AUTO_INCREMENT NOT NULL, " +
                    "original_url VARCHAR(1000) NULL, " +
                    "short_url VARCHAR(45) NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                    "PRIMARY KEY (id)) CHARACTER SET utf8 COLLATE utf8_general_ci";
            stmt.executeUpdate(createTableQuery);
            System.out.println("Table created successfully...");

            // Создание первой тестовой строки в таблице
                String insertQuery = "INSERT INTO " + tableName + " (original_url, short_url) VALUES ('https://www.example.com', 'example')";
                stmt.executeUpdate(insertQuery);
                System.out.println("First test row inserted successfully...");
                DBisExist = true;
        } catch (SQLException e) {
            e.printStackTrace();
            DBisExist = true;
        } finally {
            if (conn!= null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public boolean isDBisExist(){
        return DBisExist;
    }
}
