package DAOLayer;

import View.ViewServlet;

import javax.servlet.ServletContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class DbCreater {
    private static DbCreater instance;
    private static boolean DBisExist = false;

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
        String table1Name = props.getProperty("table1.name");
        String table2Name = props.getProperty("table2.name");

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

            // Создание таблицы users
            String createUserTableQuery = "CREATE TABLE " + table1Name + " (" +
                    "id BIGINT AUTO_INCREMENT NOT NULL, " +
                    "login VARCHAR(45) NOT NULL, " +
                    "password VARCHAR(100) NOT NULL, " +
                    "PRIMARY KEY (id)) CHARACTER SET utf8 COLLATE utf8_general_ci";
            stmt.executeUpdate(createUserTableQuery);
            System.out.println("Table users created successfully...");

            // Создание таблицы links с изменением поля updated_at
            String createLinkTableQuery = "CREATE TABLE " + table2Name + " (" +
                    "id BIGINT AUTO_INCREMENT NOT NULL, " +
                    "original_url VARCHAR(1000) NULL, " +
                    "short_url VARCHAR(45) NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "user_id BIGINT NULL, " +
                    "PRIMARY KEY (id), " +
                    "FOREIGN KEY (user_id) REFERENCES "+ table1Name +"(id)) CHARACTER SET utf8 COLLATE utf8_general_ci";
            stmt.executeUpdate(createLinkTableQuery);
            System.out.println("Table links created successfully...");

            // Создание первой тестовой строки в таблице users
            String insertUserQuery = "INSERT INTO " + table1Name + " (login, password) VALUES ('test', 'password')";
            stmt.executeUpdate(insertUserQuery);
            System.out.println("First test user inserted successfully...");

            // Получение id первого тестового пользователя
            String selectUserIdQuery = "SELECT id FROM " + table1Name + " WHERE login = 'test'";
            ResultSet rs = stmt.executeQuery(selectUserIdQuery);
            int userId = 0;
            while (rs.next()) {
                userId = rs.getInt("id");
            }
            rs.close();

            // Создание первой тестовой строки в таблице links
            String insertLinkQuery = "INSERT INTO " + table2Name +  "(original_url, short_url, user_id) VALUES ('https://www.example.com', 'example', " + userId + ")";
            stmt.executeUpdate(insertLinkQuery);
            System.out.println("First test link inserted successfully...");
            DBisExist = true;
        } catch (SQLException e) {
            if(e.getErrorCode() == 1007){
                DBisExist = true;
                System.out.println("База уже существует");
            }else{
                e.printStackTrace();
            }
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
