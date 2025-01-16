package DAOLayer;

import javax.servlet.ServletContext;
import java.io.FileInputStream;
import java.sql.*;
import java.util.Arrays;
import java.util.Properties;

import org.slf4j.*;

public class DbCreator {
    private static final Logger log = LoggerFactory.getLogger(DbCreator.class);
    //------------------------------------------------------------------
    private String dbName;
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private String table1Name;
    private String table2Name;
    private Connection conn = null;
    private Statement stmt = null;
    private boolean isDbCreated = false;
    public boolean getDbCreated(){
        return isDbCreated;
    }

    public DbCreator() {

    }
    //------------------------------------------------------------------------------------------------------------------
    // это конечно, полный бред, но ниже мы будем смотреть есть ли в стеке вызова тестовый метод, чтобы определить
    // тестовая это ошибка или реальная... (((
    public static boolean isMethodCalledFromTest() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        return Arrays.stream(stackTrace)
                .anyMatch(frame -> frame.getClassName().startsWith("org.junit")); // JUnit
    }
    //------------------------------------------------------------------------------------------------------------------
    public DbCreator(ServletContext servletContext) {
        setProps(servletContext);
    }

    //------------------------------------------------------------------------------------------------------------------
    public boolean setUpDB() {
        if (isDbCreated) {
            return true;
        }
        try {
            methodForTestSQLException();
            loadJDBCDriver();
            openConnectionToDB();
            createStatementForDB();
            createDB();
            closeConnection();
            openConnectionToSchema();
            createStatementForSchema();
            createTableUsers();
            createTableLinks();
            createTestRowInUsersTable();
            createTestRowInLinksTable();
            isDbCreated = true;
            return true;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1007) {
//                System.out.println(e.getMessage());
                if(isMethodCalledFromTest()){
                    log.debug(e.getMessage());
                } else {
                    log.info(e.getMessage());
                }
                isDbCreated = true;
                return true;
            } else if (e.getMessage().contains("Testing for checking handling exception errorCode")) {
                log.debug(e.getMessage());
                return false;
            } else {
//                e.printStackTrace();
                log.error("Error connecting to the database: " + e.getMessage());
                return false;
            }
        } finally {
            closeConnection();
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    private void setProps(ServletContext servletContext) {
        Properties props = new Properties();
        String realPath = servletContext.getRealPath("/WEB-INF/props/database.properties");

        try (FileInputStream fis = new FileInputStream(realPath)) {
            props.load(fis);
        } catch (Exception e) {
//            StackTraceElement[] stackTrace = e.getStackTrace();
//            StringBuilder sb = new StringBuilder();
//            for (StackTraceElement element : stackTrace) {
//                sb.append(element);
//            }
//            RuntimeException runtimeException;
//            if(sb.toString().contains("testSetPropsFailure")){
//                runtimeException = new RuntimeException("Tested error with load settings from file: /WEB-INF/props/database.properties", e);
//                log.debug(runtimeException.getMessage());
//            }else{
//                runtimeException = new RuntimeException("Error with load settings from file: /WEB-INF/props/database.properties", e);
//                log.warn(runtimeException.getMessage());
//            }
            RuntimeException runtimeException;
            if(isMethodCalledFromTest()){
                runtimeException = new RuntimeException("Tested error with load settings from file: /WEB-INF/props/database.properties", e);
                log.debug(runtimeException.getMessage());
            } else {
                runtimeException = new RuntimeException("Error with load settings from file: /WEB-INF/props/database.properties", e);
                log.warn(runtimeException.getMessage());
            }
            throw runtimeException;
        }
        this.dbName = props.getProperty("db.name");
        this.dbUrl = props.getProperty("db.url");
        this.dbUser = props.getProperty("db.user");
        this.dbPassword = props.getProperty("db.password");
        this.table1Name = props.getProperty("table1.name");
        this.table2Name = props.getProperty("table2.name");
    }

    private void loadJDBCDriver(){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void openConnectionToDB() throws SQLException {
        // Open a connection
        conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    private void openConnectionToSchema() throws SQLException {
        // Подключение к базе данных
        String url = dbUrl + dbName;
        conn = DriverManager.getConnection(url, dbUser, dbPassword);
        stmt = conn.createStatement();

    }

    private void createStatementForDB() throws SQLException {
        stmt = conn.createStatement();
        stmt.executeUpdate("SET NAMES utf8");
        stmt.executeUpdate("SET CHARACTER SET utf8");

    }

    private void createStatementForSchema() throws SQLException {
        stmt.executeUpdate("SET NAMES utf8");
        stmt.executeUpdate("SET CHARACTER SET utf8");
        stmt.executeUpdate("SET COLLATION_CONNECTION=utf8_general_ci");

    }

    private void createDB() throws SQLException {
        String sql = "CREATE DATABASE " + dbName + " CHARACTER SET utf8 COLLATE utf8_general_ci";
        stmt.executeUpdate(sql);
        //*
        if(isMethodCalledFromTest()){
            log.debug("Database " + dbName + " created successfully...");
        } else {
            log.info("Database " + dbName + " created successfully...");
        }

    }

    private void createTableUsers() throws SQLException {
        // Создание таблицы users
        String createUserTableQuery = "CREATE TABLE " + table1Name + " (" +
                "id BIGINT AUTO_INCREMENT NOT NULL, " +
                "login VARCHAR(45) NOT NULL, " +
                "password VARCHAR(100) NOT NULL, " +
                "PRIMARY KEY (id)) CHARACTER SET utf8 COLLATE utf8_general_ci";
        stmt.executeUpdate(createUserTableQuery);
//        System.out.println("Table " + table1Name + " created successfully... ");
        if(isMethodCalledFromTest()){
            log.debug("Table " + table1Name + " created successfully... ");
        } else {
            log.info("Table " + table1Name + " created successfully... ");
        }

    }

    private void createTableLinks() throws SQLException {
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
//        System.out.println("Table " + table2Name + " created successfully...");
        if(isMethodCalledFromTest()){
            log.debug("Table " + table2Name + " created successfully...");
        } else {
            log.info("Table " + table2Name + " created successfully...");
        }

    }

    private void createTestRowInUsersTable() throws SQLException {
        // Создание первой тестовой строки в таблице users
        String insertUserQuery = "INSERT INTO " + table1Name + " (login, password) VALUES ('test', 'password')";
        stmt.executeUpdate(insertUserQuery);
//        System.out.println("First test user inserted successfully...");
        if(isMethodCalledFromTest()){
            log.debug("First test user inserted successfully...");
        } else {
            log.info("First test user inserted successfully...");
        }

    }

    private void createTestRowInLinksTable() throws SQLException {
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
//        System.out.println("First test link inserted successfully...");
        if(isMethodCalledFromTest()){
            log.debug("First test link inserted successfully...");
        } else {
            log.info("First test link inserted successfully...");
        }

    }

    private void closeConnection(){
        if (conn!= null) {
            try {
                conn.close();
            } catch (SQLException e) {
//                e.printStackTrace();
                log.error("Error closing database connection: " + e.getMessage(), e);
            }
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    // метод только для тестов, абсолютно не функционален
    void methodForTestSQLException() throws SQLException {

    }

}
