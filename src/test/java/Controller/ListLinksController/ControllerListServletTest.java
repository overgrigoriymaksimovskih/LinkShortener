package Controller.ListLinksController;

import static org.junit.jupiter.api.Assertions.*;
import DAOLayer.ModelManager;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ControllerListServletTest {

    @Mock
    private ModelManager modelManager;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @InjectMocks
    private ControllerListServlet servlet;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    public void setup() throws IOException {
        MockitoAnnotations.openMocks(this);
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
//проверяем, что метод doGet вызывается 1 раз с нужными параметрами
    void testDoGetSuccess() throws ServletException, IOException {
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/home/controller/list"));
        servlet.doGet(request, response);
        verify(modelManager, times(1)).handleListLinks(eq(servlet), eq(response), eq(request), eq("http://localhost:8080"));
    }

    @Test
//проверяем, что метод update правильно устанавливает contentType и characterEncoding ответа.
//Что метод создает и отправляет в response JSON-ответ в правильном формате, а результат вызова метода update соответствует ожидаемому
    void testUpdateSuccess() throws Exception {
        // Задаем ожидаемую строку которую нужно будет сравнить с тем, что вернет метод update
        String expectedJson = new JSONObject()
                .put("result", "testUser")
                .put("message", "testLinks")
                .put("pageNumber", 1)
                .put("totalPages", 3)
                .toString();

        // Настраиваем мок response, чтобы возвращал наш StringWriter
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        // Вызываем метод update сервлета
        String testResult = "testUser";
        String testMessage = "testLinks";
        int testPageNumber = 1;
        int testTotalPages = 3;
        servlet.update(response, testResult, testMessage, testPageNumber, testTotalPages);

        // Проверяем contentType
        verify(response).setContentType("application/json");
        // Проверяем characterEncoding
        verify(response).setCharacterEncoding("UTF-8");

        // Проверяем JSON формат ответа
        JSONObject jsonResponse = new JSONObject(stringWriter.toString());
        assertEquals(testResult, jsonResponse.get("result"));
        assertEquals(testMessage, jsonResponse.get("message"));
        assertEquals(testPageNumber, jsonResponse.get("pageNumber"));
        assertEquals(testTotalPages, jsonResponse.get("totalPages"));

        // Проверяем что ожидаемая строка соответствует тому, что вернет метод update
        assertEquals(expectedJson, stringWriter.toString());
    }

    @Test
//проверяем ошибку IOException при отправке ответа response.getWriter()
    void testUpdateIOExceptionGetWriter() {
        //Настраиваем мок response, чтобы метод getWriter() выкинул IOException
        try {
            when(response.getWriter()).thenThrow(new IOException("Test ControllerListServlet cannot getWriter IOException ->(All OK)"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Вызываем метод update сервлета и проверяем что не будет брошено исключение
        // так как мы генерируем ошибку то должно быть напечатано сообщение с "Ошибка при отправке ответа: Test IOException"
        try {
            servlet.update(response, "error", "Test exception occurred", 1, 1);
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
            servlet.update(response, "error", "Test exception occurred", 1, 1);
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
        servlet.update(response, "success", "Operation successful", 1, 1);

        //Проверяем, что метод close() был вызван на PrintWriter
        verify(printWriterMock).close();
    }

    @Test
//проверяем закрытие printWriter c ошибкой закрытия
    void testUpdateIOExceptionPrintWriterClose(){
        //Настраиваем мок PrintWriter так, чтобы close() выбросил IOException
        PrintWriter printWriterMock = mock(PrintWriter.class);
        doThrow(new IOException("Test ControllerListServlet IOException in close ->(All OK)")).when(printWriterMock).close();

        //Настраиваем мок response так, чтобы getWriter() вернул мок PrintWriter
        try {
            when(response.getWriter()).thenReturn(printWriterMock);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //Вызываем метод update сервлета
        servlet.update(response, "error", "Test exception occurred", 1, 1);
        //Проверяем, что close() был вызван (но не ожидаем исключения в этом методе)
        verify(printWriterMock).close();
    }
}