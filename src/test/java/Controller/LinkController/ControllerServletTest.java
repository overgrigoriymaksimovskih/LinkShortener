package Controller.LinkController;

import DAOLayer.ModelManager;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class ControllerServletTest {
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private ModelManager modelManager;
    @Mock
    private LineHandler lineHandler;

    @Mock
    private HttpSession session;
    @InjectMocks
    private ControllerServlet controllerServlet;
    private StringWriter stringWriter;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(request.getSession()).thenReturn(session);
        stringWriter = new StringWriter();
    }

    //--------------------------------------------------------------------------------------------------------------init
    @Test
//проверяем, что наша стандартная реализация метода init не вызывает исключений (она и не должна, но вдруг потом
// в него добавится какой то специфический функционал... этот метод не должен вызывать исключений)
    public void testInit(){
        try {
            controllerServlet.init();
        } catch (Exception e) {
            fail("Метод init() выбросил исключение: " + e.getMessage());
        }
    }
    //-------------------------------------------------------------------------------------------------------------doGet
    @Test
//Проверяем что контроллер корректно обрабатывает пустые строки
    public void testDoGetEmptyLine() throws Exception {
        // 1. Настройка моков:
        when(lineHandler.isNotEmpty(request)).thenReturn(false);
        // Когда у lineHandler спросят поустая ли строка, он ответит "Мамой клянус пустая"
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        // Когда у response попросят writer, чтобы туда записать ответ от метода update в классе ControllerServlet мы
        //отдадим этот врайтер, вот ниже прям так и напишем
        when(response.getWriter()).thenReturn(writer);


        // 2. Вызов метода doGet:
        controllerServlet.doGet(request, response);
        //тут мы запихиваем в наш метод ДоГет (который в моке controllerServlet) наш мок респонса который говорит что
        // строка пустая, а ответ, говорит:"запишите пожалуйста в вот этот врайтер" (который мы чуть выше создали) и мок
        // реквеста который просто мок клсса HttpServletRequest

        // 3. Проверки:
        verify(lineHandler, times(1)).isNotEmpty(request);    // Проверяем, что isEmpty был вызван 1 раз
        verify(lineHandler, times(0)).isLink(request);    // Проверяем, что isLink НЕ вызывался
        verify(modelManager, times(0)).handleLink(any(), any(), any(), any(), any()); // Проверяем, что handleLink НЕ вызывался

        // 4. Проверка ответа
        String expectedResponse = "{\"huy\":\"Вы не ввели строку\"}";
        assertEquals(expectedResponse, stringWriter.toString());
    }

    @Test
//Проверяем что контроллер корректно обрабатывает не корректные ссылки
//в принципе все по аналогии с пустой строкой...
    public void testDoGetInvalidLink() throws Exception {
        when(lineHandler.isNotEmpty(request)).thenReturn(true);
        when(lineHandler.isLink(request)).thenReturn(false);
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        controllerServlet.doGet(request, response);
        verify(lineHandler, times(1)).isNotEmpty(request);
        verify(lineHandler, times(1)).isLink(request);
        verify(modelManager, times(0)).handleLink(any(), any(), any(), any(), any());

        String expectedResponse = "{\"huy\":\"Введенная строка не является ссылкой\"}";
        assertEquals(expectedResponse, stringWriter.toString());

    }

    @Test
//Проверяем что контроллер корректно обрабатывает корректные ссылки
//первая часть аналогично предыдущим тестам
    public void testDoGetValidLink() throws Exception {
        when(lineHandler.isNotEmpty(request)).thenReturn(true);
        when(lineHandler.isLink(request)).thenReturn(true);
        when(lineHandler.resetProtocol(request)).thenReturn("some_link");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/home/controller"));

        controllerServlet.doGet(request, response);

        verify(lineHandler, times(1)).isNotEmpty(request);
        verify(lineHandler, times(1)).isLink(request);
        verify(modelManager, times(1)).handleLink(eq(controllerServlet),eq(response),eq("some_link"),eq("http://localhost:8080"), eq(request));
    //вторая часть отсутствует, потому что обработка переданной ссылки происходит в другом классе, для него мы будем
    //писать свои тесты, а тут достаточно проверить, что его метод для обработки ссылки был вызван 1 раз с корректными параметрами
    }

    //------------------------------------------------------------------------------------------------------------update
    @Test
//проверяем, что метод update правильно устанавливает contentType и characterEncoding ответа.
//Что метод создает и отправляет в response JSON-ответ в правильном формате, а результат вызова метода update соответствует ожидаемому
    void testUpdateSuccess() throws Exception {
        // Задаем ожидаемую строку которую нужно будет сравнить с тем, что вернет метод update
        String expectedJson = new JSONObject()
                .put("huy", "success")
                .toString();

        // Настраиваем мок response, чтобы возвращал наш StringWriter
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        // Вызываем метод update сервлета
        String testResult = "success";
        String testMessage = "success";
        controllerServlet.update(response, testResult, testMessage);

        // Проверяем contentType
        verify(response).setContentType("application/json");
        // Проверяем characterEncoding
        verify(response).setCharacterEncoding("UTF-8");

        // Проверяем JSON формат ответа
        JSONObject jsonResponse = new JSONObject(stringWriter.toString());
        assertEquals(testResult, jsonResponse.get("huy"));

        // Проверяем что ожидаемая строка соответствует тому, что вернет метод update
        assertEquals(testMessage, jsonResponse.get("huy"));
    }

    @Test
//проверяем ошибку IOException при отправке ответа response.getWriter()
    void testUpdateIOExceptionGetWriter() {
        //Настраиваем мок response, чтобы метод getWriter() выкинул IOException
        try {
            when(response.getWriter()).thenThrow(new IOException("Test ControllerServlet cannot getWriter IOException ->(All OK)"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Вызываем метод update сервлета и проверяем что не будет брошено исключение
        // так как мы генерируем ошибку то должно быть напечатано сообщение с "Ошибка при отправке ответа: Test IOException"
        try {
            controllerServlet.update(response, "error", "Test exception occurred");
        } catch (Throwable e) {
            org.junit.jupiter.api.Assertions.fail("Это исключение не должно было быть выброшено: " + e.getMessage());
        }
    }

    //проверяем ошибку IOException при отправке ответа response.getWriter() возвращающий null
    @Test
    void testUpdateResponseWriterNull() {
        //Настраиваем мок response, чтобы метод getWriter() вернул null
        try {
            when(response.getWriter()).thenReturn(null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //Вызываем метод update сервлета и проверяем что не будет брошено исключение
        // так как мы генерируем ошибку то должно быть напечатано сообщение с "Ошибка при отправке ответа: Test IOException"
        try {
            controllerServlet.update(response, "error", "Test exception occurred");
        } catch (Throwable e) {
            org.junit.jupiter.api.Assertions.fail("Это исключение не должно было быть выброшено: " + e.getMessage());
        }
        //Assert: verify that the update method does not throw NullPointerException
        assertTrue(true);
    }
    @Test
//проверяем закрытие printWriter при успешном выполнении
    void testUpdateSuccessPrintWriterClose(){
        //Настраиваем мок response, чтобы возвращал мок PrintWriter
        PrintWriter printWriterMock = mock(PrintWriter.class);
        try {
            //когда берем врайтер у нашего респонса получаем МОК класса PrintWriter
            when(response.getWriter()).thenReturn(printWriterMock);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Вызываем метод update сервлета (все должно пройти успешно)
        //там внутри тоже врайтер берется из респонса, а мы уже сказали что при запросе принтврайтера надо отдавать его МОК
        //поэтому тут тоже взаимодействуем с МОКОМ
        controllerServlet.update(response, "success", "Operation successful");

        //Проверяем, что метод close() был вызван на PrintWriter
        verify(printWriterMock).close();
    }

    @Test
//проверяем закрытие printWriter c ошибкой закрытия
    void testUpdateIOExceptionPrintWriterClose(){
        //Настраиваем мок PrintWriter так, чтобы close() выбросил IOException
        PrintWriter printWriterMock = mock(PrintWriter.class);
        doThrow(new IOException("Test ControllerServlet IOException in close ->(All OK)")).when(printWriterMock).close();

        //Настраиваем мок response так, чтобы getWriter() вернул мок PrintWriter
        try {
            when(response.getWriter()).thenReturn(printWriterMock);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //Вызываем метод update сервлета
        controllerServlet.update(response, "TestResult", "TestMessage");
        //Проверяем, что close() был вызван (но не ожидаем исключения в этом методе)
        verify(printWriterMock).close();
    }
}