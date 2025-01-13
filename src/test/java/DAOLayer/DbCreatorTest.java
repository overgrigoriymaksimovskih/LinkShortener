package DAOLayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DbCreatorTest {
    private ServletContext servletContextMock;
    private String databasePropertiesPath;
    private String realPath;
    private DbCreator dbCreator;

    @BeforeEach
    void setUp() throws UnsupportedEncodingException {
        dbCreator = new DbCreator();

    }
    private Object getField(Object target, String fieldName) throws IllegalAccessException, NoSuchFieldException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }
    public void setStringField(Object target, String fieldName, String value) throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
    private void setObjectField(Object target, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
    public void setBooleanField(Object target, String fieldName, boolean value) throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    //------------------------------------------------------------------------------------------------------------------
    @Test
    void testSetPropsSuccessful() throws Exception{
        servletContextMock = Mockito.mock(ServletContext.class);
        databasePropertiesPath = "DAOLayer/props/test_database.properties";

        // Получаем полный путь к файлу (URL)
        URL resourceUrl = getClass().getClassLoader().getResource(databasePropertiesPath);
        if(resourceUrl == null){
            throw new NullPointerException("файл properties для теста не найден");
        }
        // Декодирование URL
        String decodedPath = URLDecoder.decode(resourceUrl.getFile(), StandardCharsets.UTF_8.name());
        File resourceFile = new File(decodedPath);
        realPath = resourceFile.getAbsolutePath();

        when(servletContextMock.getRealPath("/WEB-INF/props/database.properties")).thenReturn(realPath);

        // 1. Создаем экземпляр тестируемого класса
        assertNull(getField(dbCreator,"dbName"), "dbName должно быть null");

        // 2. Получаем доступ к приватному методу
        Method setPropsMethod = DbCreator.class.getDeclaredMethod("setProps", ServletContext.class);
        setPropsMethod.setAccessible(true);

        // 3. Вызываем приватный метод через рефлексию
        setPropsMethod.invoke(dbCreator, servletContextMock);
        assertEquals(getField(dbCreator,"dbName"), "testdb", "privateString должно быть равно testdb");

        // 4. Проверяем результат
        Field dbNameField = DbCreator.class.getDeclaredField("dbName");
        dbNameField.setAccessible(true);
        assertEquals(dbNameField.get(dbCreator), "testdb");
    }
    @Test
    void testSetPropsFailure() throws Exception {
        servletContextMock = Mockito.mock(ServletContext.class);
        // 1. Настраиваем мок ServletContext для имитации ошибки загрузки файла.
        when(servletContextMock.getResourceAsStream(databasePropertiesPath)).thenReturn(null);

        // 2. Получаем доступ к приватному методу
        Method setPropsMethod = DbCreator.class.getDeclaredMethod("setProps", ServletContext.class);
        setPropsMethod.setAccessible(true);

        // 3. Проверяем что метод выбрасывает правильное исключение
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            try {
                setPropsMethod.invoke(dbCreator, servletContextMock);
            } catch (InvocationTargetException e) {
                // Распаковываем исключение
                Throwable cause = e.getCause();
                if(cause instanceof RuntimeException){
                    throw (RuntimeException) cause;
                }
                else{
                    throw e;
                }
            }
        });
        // 4. Проверяем сообщение исключения
        assertTrue(exception.getMessage().contains("Тестовая ошибка при загрузке настроек из файла: /WEB-INF/props/database.properties"));
    }
    //------------------------------------------------------------------------------------------------------------------
    @Test
    void testLoadJDBCDriverSuccessful() throws Exception {
        // 1. Получаем доступ к приватному методу
        Method loadJDBCDriverMethod = DbCreator.class.getDeclaredMethod("loadJDBCDriver");
        loadJDBCDriverMethod.setAccessible(true);

        // 3. Вызываем приватный метод через рефлексию и убеждаемся что он не вызвал исключений
        assertDoesNotThrow(() -> loadJDBCDriverMethod.invoke(dbCreator));

    }
    @Test
    void testLoadJDBCDriverFailure() {
        assertThrows(ClassNotFoundException.class, () -> {
            try {
                // 1. Получаем метод loadJDBCDriver
                Method loadJDBCDriverMethod = DbCreator.class.getDeclaredMethod("loadJDBCDriver");
                loadJDBCDriverMethod.setAccessible(true);

                // 2. Заменяем имя класса, чтобы вызвать исключение
                Class.forName("some.incorrect.ClassName");
                // 3.  вызываем метод loadJDBCDriver - тут будет брошено исключение если что-то пойдет не так.
                loadJDBCDriverMethod.invoke(dbCreator);
            }
            // 4. Перехватываем и снова выбрасываем исключение, чтобы `assertThrows` смогло его поймать
            catch (ClassNotFoundException e) {
                throw e;
            }
        });
    }
    //------------------------------------------------------------------------------------------------------------------
    @Test
    void testOpenConnectionToDBSuccessful() throws Exception {
        setStringField(dbCreator, "dbUrl", "a");
        setStringField(dbCreator, "dbUser", "b");
        setStringField(dbCreator, "dbPassword", "c");
        try (MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class)) {
            Connection mockedConnection = Mockito.mock(Connection.class);
            when(DriverManager.getConnection("a", "b", "c")).thenReturn(mockedConnection);

            // 1. Создаем экземпляр тестируемого класса
            assertNull(getField(dbCreator, "conn"), "conn должно быть null");

            // 2. Получаем доступ к приватному методу
            Method openConnectionToDBMethod = DbCreator.class.getDeclaredMethod("openConnectionToDB");
            openConnectionToDBMethod.setAccessible(true);

            // 3. Вызываем приватный метод через рефлексию
            openConnectionToDBMethod.invoke(dbCreator);
            assertNotNull(getField(dbCreator, "conn"), "privateString должно быть not null");

            // 4. Проверяем результат
            Field connField = DbCreator.class.getDeclaredField("conn");
            connField.setAccessible(true);
            assertTrue(connField.get(dbCreator) instanceof Connection, "Поле conn должно быть типа Connection");
            assertSame(mockedConnection, connField.get(dbCreator), "Поле conn должно содержать наш замоканный объект");
        }
    }
    //------------------------------------------------------------------------------------------------------------------
    @Test
    void testOpenConnectionToSchemaSuccessfu() throws Exception {
        setStringField(dbCreator, "dbName", "a");
        setStringField(dbCreator, "dbUrl", "a");
        setStringField(dbCreator, "dbUser", "b");
        setStringField(dbCreator, "dbPassword", "c");
        try (MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class)) {
            Connection mockedConnection = Mockito.mock(Connection.class);
            when(DriverManager.getConnection("aa", "b", "c")).thenReturn(mockedConnection);

            // 1. Создаем экземпляр тестируемого класса
            assertNull(getField(dbCreator, "conn"), "conn должно быть null");

            // 2. Получаем доступ к приватному методу
            Method openConnectionToSchemaMethod = DbCreator.class.getDeclaredMethod("openConnectionToSchema");
            openConnectionToSchemaMethod.setAccessible(true);

            // 3. Вызываем приватный метод через рефлексию
            openConnectionToSchemaMethod.invoke(dbCreator);
            assertNotNull(getField(dbCreator, "conn"), "privateString должно быть not null");

            // 4. Проверяем результат
            Field connField = DbCreator.class.getDeclaredField("conn");
            connField.setAccessible(true);
            assertTrue(connField.get(dbCreator) instanceof Connection, "Поле conn должно быть типа Connection");
            assertSame(mockedConnection, connField.get(dbCreator), "Поле conn должно содержать наш замоканный объект");
        }
    }
    //------------------------------------------------------------------------------------------------------------------
    @Test
    void testCreateStatementForDBSuccessful() throws Exception {
        // 1. Настраиваем моки
        Connection mockedConnection = Mockito.mock(Connection.class);
        Statement mockedStatement = Mockito.mock(Statement.class);

        when(mockedConnection.createStatement()).thenReturn(mockedStatement);

        // 2. Настраиваем поля объекта DbCreator. Задаём значение conn моком.
        setObjectField(dbCreator, "conn", mockedConnection);

        // 3. Вызываем метод
        Method createStatementForDBMethod = DbCreator.class.getDeclaredMethod("createStatementForDB");
        createStatementForDBMethod.setAccessible(true);
        assertDoesNotThrow(() -> createStatementForDBMethod.invoke(dbCreator));
        // 4. Проверяем результат
        Field stmtField = DbCreator.class.getDeclaredField("stmt");
        stmtField.setAccessible(true);
        assertNotNull(stmtField.get(dbCreator), "Поле stmt не должно быть null");
        assertTrue(stmtField.get(dbCreator) instanceof Statement, "Поле stmt должно быть типа Statement");
        assertSame(mockedStatement, stmtField.get(dbCreator), "Поле stmt должно содержать наш замоканный объект");
    }
    //------------------------------------------------------------------------------------------------------------------
    @Test
    void testCreateStatementForSchemaSuccessful() throws Exception {
        // 1. Настраиваем моки
        Statement mockedStatement = Mockito.mock(Statement.class);
        // 2. Устанавливаем значение поля
        setObjectField(dbCreator, "stmt", mockedStatement);

        // 3. Вызываем метод
        Method createStatementForSchemaMethod = DbCreator.class.getDeclaredMethod("createStatementForSchema");
        createStatementForSchemaMethod.setAccessible(true);
        assertDoesNotThrow(() ->  createStatementForSchemaMethod.invoke(dbCreator));

        // 4. Проверяем что executeUpdate вызывается три раза
        verify(mockedStatement, times(3)).executeUpdate(anyString());
    }
    //------------------------------------------------------------------------------------------------------------------
    @Test
    void testCreateDBSuccessful() throws Exception {

        String dbName = "TESTDATABASE";

        // Мокируем Statement
        Statement mockedStatement = Mockito.mock(Statement.class);

        // Устанавливаем значение stmt
        setObjectField(dbCreator, "stmt", mockedStatement);

        // Задаем поведение для executeUpdate
        when(mockedStatement.executeUpdate(anyString())).thenReturn(0);

        // Устанавливаем значение dbName
        setStringField(dbCreator, "dbName", dbName);

        // Вызываем метод
        Method createDBMethod = DbCreator.class.getDeclaredMethod("createDB");
        createDBMethod.setAccessible(true);
        assertDoesNotThrow(() -> createDBMethod.invoke(dbCreator));

        // Проверяем, что executeUpdate был вызван с правильным SQL
        verify(mockedStatement).executeUpdate("CREATE DATABASE " + dbName + " CHARACTER SET utf8 COLLATE utf8_general_ci");
    }
    //------------------------------------------------------------------------------------------------------------------
    @Test
    void testCreateTableUsersSuccessful() throws Exception {
        String table1Name = "TESTUSERSTABLE";
        // 1. Мокируем Statement
        Statement mockedStatement = Mockito.mock(Statement.class);

        // 2. Устанавливаем значение поля stmt
        setObjectField(dbCreator, "stmt", mockedStatement);

        // 3. Устанавливаем значение table1Name
        setStringField(dbCreator, "table1Name", table1Name);

        // 4. Задаем поведение для executeUpdate
        when(mockedStatement.executeUpdate(anyString())).thenReturn(0);


        // 5. Вызываем метод
        Method createTableUsersMethod = DbCreator.class.getDeclaredMethod("createTableUsers");
        createTableUsersMethod.setAccessible(true);
        assertDoesNotThrow(() -> createTableUsersMethod.invoke(dbCreator));
        // 6. Проверяем, что executeUpdate был вызван с правильным SQL

        String createUserTableQuery = "CREATE TABLE " + table1Name + " (" +
                "id BIGINT AUTO_INCREMENT NOT NULL, " +
                "login VARCHAR(45) NOT NULL, " +
                "password VARCHAR(100) NOT NULL, " +
                "PRIMARY KEY (id)) CHARACTER SET utf8 COLLATE utf8_general_ci";

        verify(mockedStatement).executeUpdate(createUserTableQuery);

    }
    //------------------------------------------------------------------------------------------------------------------
    @Test
    void testCreateTableLinksSuccessful() throws Exception {

        String table1Name = "users";
        String table2Name = "TESTLINKSTABLE";

        // 1. Мокируем Statement
        Statement mockedStatement = Mockito.mock(Statement.class);

        // 2. Устанавливаем значение поля stmt
        setObjectField(dbCreator, "stmt", mockedStatement);

        // 3. Устанавливаем значение table1Name и table2Name
        setStringField(dbCreator, "table1Name", table1Name);
        setStringField(dbCreator, "table2Name", table2Name);

        // 4. Задаем поведение для executeUpdate
        when(mockedStatement.executeUpdate(anyString())).thenReturn(0);

        // 5. Вызываем метод
        Method createTableLinksMethod = DbCreator.class.getDeclaredMethod("createTableLinks");
        createTableLinksMethod.setAccessible(true);
        assertDoesNotThrow(() -> createTableLinksMethod.invoke(dbCreator));


        // 6. Проверяем, что executeUpdate был вызван с правильным SQL
        String createLinkTableQuery = "CREATE TABLE " + table2Name + " (" +
                "id BIGINT AUTO_INCREMENT NOT NULL, " +
                "original_url VARCHAR(1000) NULL, " +
                "short_url VARCHAR(45) NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "user_id BIGINT NULL, " +
                "PRIMARY KEY (id), " +
                "FOREIGN KEY (user_id) REFERENCES "+ table1Name +"(id)) CHARACTER SET utf8 COLLATE utf8_general_ci";

        verify(mockedStatement).executeUpdate(createLinkTableQuery);
    }
    //------------------------------------------------------------------------------------------------------------------
    @Test
    void testCreateTestRowInUsersTableSuccessful() throws Exception {

        String table1Name = "users";

        // 1. Мокируем Statement
        Statement mockedStatement = Mockito.mock(Statement.class);

        // 2. Устанавливаем значение поля stmt
        setObjectField(dbCreator, "stmt", mockedStatement);

        // 3. Устанавливаем значение table1Name
        setStringField(dbCreator, "table1Name", table1Name);

        // 4. Задаем поведение для executeUpdate
        when(mockedStatement.executeUpdate(anyString())).thenReturn(0);

        // 5. Вызываем метод
        Method createTestRowInUsersTableMethod = DbCreator.class.getDeclaredMethod("createTestRowInUsersTable");
        createTestRowInUsersTableMethod.setAccessible(true);
        assertDoesNotThrow(() -> createTestRowInUsersTableMethod.invoke(dbCreator));


        // 6. Проверяем, что executeUpdate был вызван с правильным SQL
        String insertUserQuery = "INSERT INTO " + table1Name + " (login, password) VALUES ('test', 'password')";
        verify(mockedStatement).executeUpdate(insertUserQuery);
    }
    //------------------------------------------------------------------------------------------------------------------
    @Test
    void testCreateTestRowInLinksTableSuccessful() throws Exception {
        String table1Name = "users";
        String table2Name = "links";
        int userId = 1;

        // 1. Мокируем Statement
        Statement mockedStatement = Mockito.mock(Statement.class);
        ResultSet mockedResultSet = Mockito.mock(ResultSet.class);

        // 2. Устанавливаем значения полей stmt, table1Name, table2Name
        setObjectField(dbCreator, "stmt", mockedStatement);
        setStringField(dbCreator, "table1Name", table1Name);
        setStringField(dbCreator, "table2Name", table2Name);

        // 3. Задаем поведение для executeQuery
        when(mockedStatement.executeQuery(anyString())).thenReturn(mockedResultSet);
        when(mockedResultSet.next()).thenReturn(true, false); // один раз true, потом false
        when(mockedResultSet.getInt("id")).thenReturn(userId);

        // 4. Задаем поведение для executeUpdate
        when(mockedStatement.executeUpdate(anyString())).thenReturn(0);

        // 5. Вызываем метод
        Method createTestRowInLinksTableMethod = DbCreator.class.getDeclaredMethod("createTestRowInLinksTable");
        createTestRowInLinksTableMethod.setAccessible(true);
        assertDoesNotThrow(() -> createTestRowInLinksTableMethod.invoke(dbCreator));

        // 6. Проверяем что методы executeQuery и executeUpdate были вызваны с правильными SQL
        String selectUserIdQuery = "SELECT id FROM " + table1Name + " WHERE login = 'test'";
        verify(mockedStatement).executeQuery(selectUserIdQuery);

        String insertLinkQuery = "INSERT INTO " + table2Name +  "(original_url, short_url, user_id) VALUES ('https://www.example.com', 'example', " + userId + ")";
        verify(mockedStatement).executeUpdate(insertLinkQuery);
    }
    //------------------------------------------------------------------------------------------------------------------
    @Test
    void testCloseConnection_Successful() throws Exception {
        // 1. Мокируем Connection
        Connection mockedConnection = Mockito.mock(Connection.class);

        // 2. Устанавливаем значение поля conn
        setObjectField(dbCreator, "conn", mockedConnection);

        // 3. Вызываем метод
        Method closeConnectionMethod = DbCreator.class.getDeclaredMethod("closeConnection");
        closeConnectionMethod.setAccessible(true);
        assertDoesNotThrow(() -> closeConnectionMethod.invoke(dbCreator));

        // 4. Проверяем что метод close() был вызван
        verify(mockedConnection).close();
    }
    @Test
    void testCloseConnection_WithNullConnection() throws Exception {
        // 1. Убеждаемся, что conn null
        setObjectField(dbCreator, "conn", null);

        // 2. Вызываем метод
        Method closeConnectionMethod = DbCreator.class.getDeclaredMethod("closeConnection");
        closeConnectionMethod.setAccessible(true);
        assertDoesNotThrow(() -> closeConnectionMethod.invoke(dbCreator));

        // 3. Проверяем, что метод close() не был вызван
        verifyNoInteractions(mock(Connection.class)); // Важный момент
    }
    //------------------------------------------------------------------------------------------------------------------
    @Test
    void testSetupAlreadyExistDB() throws Exception{
        // Устанавливаем значение isDbCreated
        setBooleanField(dbCreator, "isDbCreated", true);
        // 2. Вызываем метод setUpDB()
        Method setUpDBMethod = DbCreator.class.getDeclaredMethod("setUpDB");
        setUpDBMethod.setAccessible(true);
        boolean result = (boolean) setUpDBMethod.invoke(dbCreator);
        // 3. Проверяем результат
        assertTrue(result, "Метод setUpDB должен вернуть true, когда isDbCreated = true");
    }
//    @Test
//    void testSetupDB_SQLException_ErrorCode1007() throws Exception {
//        // 1. Мокируем SQLException с кодом 1007
//        SQLException mockedException = new SQLException("Database already exists", null, 1007);
//
//        // 2. Создаем шпиона dbCreator
//        DbCreator spyDbCreator = Mockito.spy(dbCreator);
//
//        // 3. Мокируем DriverManager.getConnection() с помощью MockedStatic
//        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
//            when(DriverManager.getConnection(anyString(), anyString(), anyString())).thenThrow(mockedException);
//        }
//        //4. Мокируем метод, чтобы  не выполнилась реальная попытка подключения к БД
//
//        // 1. Получаем доступ к приватному методу
//        Method loadJDBCDriverMethod = DbCreator.class.getDeclaredMethod("loadJDBCDriver");
//        loadJDBCDriverMethod.setAccessible(true);
//
////        doNothing().when(spyDbCreator).loadJDBCDriver();
//
//        // 5. Вызываем setUpDB()
//        Method setUpDBMethod = DbCreator.class.getDeclaredMethod("setUpDB");
//        setUpDBMethod.setAccessible(true);
//        boolean result = (boolean) setUpDBMethod.invoke(spyDbCreator);
//
//        // 6. Проверяем результат
//        assertTrue(result, "Метод setUpDB должен вернуть true, когда SQLException с кодом 1007");
//        assertTrue(getField(spyDbCreator, "isDbCreated") instanceof Boolean);
//        assertTrue(getField(spyDbCreator, "isDbCreated").equals(true));
//    }

    @Test
    public void testDatabaseAlreadyExists() throws SQLException, NoSuchFieldException, IllegalAccessException {
        // 1. Создаем объект DbCreator
        DbCreator dbCreator = new DbCreator();

        // 2. Создаем spy-объект
        DbCreator spyDbCreator = spy(dbCreator);

        // 3. Получаем приватное поле isDbCreated через рефлексию
        Field isDbCreatedField = DbCreator.class.getDeclaredField("isDbCreated");
        isDbCreatedField.setAccessible(true); // Делаем поле доступным

        // 4. Устанавливаем значение isDbCreated в false
        isDbCreatedField.set(spyDbCreator, false);


        // 5. Создаем SQLException, которую мы будем бросать
        SQLException sqlException = new SQLException("Тестовая проверка обработки исключения errorCode = 1007", "00000", 1007);

        // 6. Мокируем приватный метод methodForTestSQLException(), чтобы он выбрасывал SQLException
        doThrow(sqlException).when(spyDbCreator).methodForTestSQLException();


        // 7. Вызываем setUpDB() и проверяем результат
        assertTrue(spyDbCreator.setUpDB());
        assertTrue(spyDbCreator.getDbCreated()); // Проверяем, что isDbCreated установлен в true
    }

    @Test
    public void testDatabaseCriticalError() throws SQLException, NoSuchFieldException, IllegalAccessException {
        // 1. Создаем объект DbCreator
        DbCreator dbCreator = new DbCreator();

        // 2. Создаем spy-объект
        DbCreator spyDbCreator = spy(dbCreator);

        // 3. Получаем приватное поле isDbCreated через рефлексию
        Field isDbCreatedField = DbCreator.class.getDeclaredField("isDbCreated");
        isDbCreatedField.setAccessible(true); // Делаем поле доступным

        // 4. Устанавливаем значение isDbCreated в false
        isDbCreatedField.set(spyDbCreator, false);


        // 5. Создаем SQLException, которую мы будем бросать
        SQLException sqlException = new SQLException("Тестовая проверка обработки исключения errorCode != 1007", "00000", 1002);

        // 6. Мокируем приватный метод methodForTestSQLException(), чтобы он выбрасывал SQLException
        doThrow(sqlException).when(spyDbCreator).methodForTestSQLException();


        // 7. Вызываем setUpDB() и проверяем результат
        assertFalse(spyDbCreator.setUpDB());
        assertFalse(spyDbCreator.getDbCreated()); // Проверяем, что isDbCreated установлен в true
    }





}