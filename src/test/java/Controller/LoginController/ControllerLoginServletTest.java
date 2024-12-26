package Controller.LoginController;

import static org.junit.jupiter.api.Assertions.*;
import DAOLayer.ModelManager;
import org.json.JSONException;
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

public class ControllerLoginServletTest {

    @Mock
    private ModelManager modelManager;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;
    @InjectMocks
    private ControllerLoginServlet servlet;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    public void setup() throws IOException {
        MockitoAnnotations.openMocks(this);
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
//        when(request.getSession()).thenReturn(session);

    }

    @Test
    void testDoPostLogin() {
        when(request.getParameter("action")).thenReturn(null);
        servlet.doPost(request, response);
        verify(modelManager, times(1)).handleLogin(eq(servlet), eq(response), eq(request));
    }
    @Test
    void testDoPostLogout() {
        when(request.getParameter("action")).thenReturn("logout");
        servlet.doPost(request, response);
        verify(modelManager, times(1)).handleLogout(eq(servlet), eq(request), eq(response));
    }

    @Test
//проверяем, что метод update правильно устанавливает contentType и characterEncoding ответа.
//Что метод создает и отправляет в response JSON-ответ в правильном формате, а результат вызова метода update соответствует ожидаемому
    void testUpdateSuccess() throws Exception {
        // Задаем ожидаемую строку которую нужно будет сравнить с тем, что вернет метод update
        String expectedJson = new JSONObject()
                .put("result", "success")
                .put("message", "Operation successful")
                .toString();

        // Настраиваем мок response, чтобы возвращал наш StringWriter
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        // Вызываем метод update сервлета
        String testResult = "success";
        String testMessage = "Operation successful";
        servlet.update(response, testResult, testMessage);

        // Проверяем contentType
        verify(response).setContentType("application/json");
        // Проверяем characterEncoding
        verify(response).setCharacterEncoding("UTF-8");

        // Проверяем JSON формат ответа
        JSONObject jsonResponse = new JSONObject(stringWriter.toString());
        assertEquals(testResult, jsonResponse.get("result"));
        assertEquals(testMessage, jsonResponse.get("message"));

        // Проверяем что ожидаемая строка соответствует тому, что вернет метод update
        assertEquals(expectedJson, stringWriter.toString());
    }

    @Test
//проверяем ошибку IOException при отправке ответа response.getWriter()
    void testUpdateIOExceptionGetWriter() {
        //Настраиваем мок response, чтобы метод getWriter() выкинул IOException
        try {
            when(response.getWriter()).thenThrow(new IOException("Test ControllerLoginServlet cannot getWriter IOException ->(All OK)"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Вызываем метод update сервлета и проверяем что не будет брошено исключение
        // так как мы генерируем ошибку то должно быть напечатано сообщение с "Ошибка при отправке ответа: Test IOException"
        try {
            servlet.update(response, "error", "Test exception occurred");
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
            servlet.update(response, "error", "messageForTestNullPointerException");
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
        servlet.update(response, "success", "Operation successful");

        //Проверяем, что метод close() был вызван на PrintWriter
        verify(printWriterMock).close();
    }

    @Test
//проверяем закрытие printWriter c ошибкой закрытия
    void testUpdateIOExceptionPrintWriterClose(){
        //Настраиваем мок PrintWriter так, чтобы close() выбросил IOException
        PrintWriter printWriterMock = mock(PrintWriter.class);
        doThrow(new IOException("Test ControllerLoginServlet IOException in close ->(All OK)")).when(printWriterMock).close();

        //Настраиваем мок response так, чтобы getWriter() вернул мок PrintWriter
        try {
            when(response.getWriter()).thenReturn(printWriterMock);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //Вызываем метод update сервлета
        servlet.update(response, "TestResult", "TestMessage");
        //Проверяем, что close() был вызван (но не ожидаем исключения в этом методе)
        verify(printWriterMock).close();
    }
}