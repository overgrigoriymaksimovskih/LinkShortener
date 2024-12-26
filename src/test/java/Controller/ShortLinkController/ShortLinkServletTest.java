package Controller.ShortLinkController;

import static org.junit.jupiter.api.Assertions.*;

import DAOLayer.ModelManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ShortLinkServletTest {

    @Mock
    private HttpServletRequest req;

    @Mock
    private HttpServletResponse resp;

    @Mock
    private ModelManager modelManager;

    @InjectMocks
    private ShortLinkServlet servlet;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        servlet.modelManager = modelManager; // Inject mock ModelManager
    }

    @Test
    void testSuccessfulRedirect() throws IOException {
        // Задаем короткую и оригинальную ссылки
        String shortLink = "testShortLink";
        String originalLink = "http://example.com";
        // Указываем нашему моку реквеста что возвращать при запросах
        when(req.getRequestURI()).thenReturn("/contextPath/s/" + shortLink);
        when(req.getContextPath()).thenReturn("/contextPath");
        when(req.getServletPath()).thenReturn("/s");
        // Указываем нашему моку модельменеджера вернуть екзампле ком при запросе оригинальной ссылки
        when(modelManager.getOriginalLink(shortLink)).thenReturn(originalLink);

        // вызываем метод догет нашего мока сервлета с нашими моками запроса и ответа
        servlet.doGet(req, resp);

        // проверяем, сто метод вызвался с правильными параметрами и ответу не задавался статус нотфоунд
        verify(resp).sendRedirect(originalLink);
        verify(resp, never()).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    void testShortLinkNotFound() throws IOException {
        //задаем короткую ссылку (оригинальная не понадобится), дальше по аналогии но возвращаем нулл
        String shortLink = "nonexistentShortLink";
        when(req.getRequestURI()).thenReturn("/contextPath/s/" + shortLink);
        when(req.getContextPath()).thenReturn("/contextPath");
        when(req.getServletPath()).thenReturn("/s");
        when(modelManager.getOriginalLink(shortLink)).thenReturn(null);

        // вызываем метод догет нашего мока сервлета с нашими моками запроса и ответа
        servlet.doGet(req, resp);

        // проверяем, что ответу задавался статус нотфоунд и никакая строка не вернулась
        verify(resp).setStatus(HttpServletResponse.SC_NOT_FOUND);
        verify(resp, never()).sendRedirect(anyString());
    }


    @Test
    void testIOExceptionDuringRedirect() throws IOException {
        //аналогично
        String shortLink = "testShortLink";
        String originalLink = "http://example.com";
        when(req.getRequestURI()).thenReturn("/contextPath/s/" + shortLink);
        when(req.getContextPath()).thenReturn("/contextPath");
        when(req.getServletPath()).thenReturn("/s");
        when(modelManager.getOriginalLink(shortLink)).thenReturn(originalLink);
        doThrow(new IOException("Simulated IOException")).when(resp).sendRedirect(originalLink);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> servlet.doGet(req, resp));

        assertTrue(exception.getCause() instanceof IOException);
        assertEquals("Simulated IOException", exception.getCause().getMessage());
    }

    @Test
    void testEmptyShortLink() throws IOException {
        // аналогично
        when(req.getRequestURI()).thenReturn("/contextPath/s/");
        when(req.getContextPath()).thenReturn("/contextPath");
        when(req.getServletPath()).thenReturn("/s");
        when(modelManager.getOriginalLink("")).thenReturn("testLink");

        // Act
        servlet.doGet(req, resp);
        // Assert
        verify(resp).sendRedirect("testLink");

    }
}