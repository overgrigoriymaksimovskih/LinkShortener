package Controller.LinkController;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.when;


public class LineHandlerTest {

    @Mock
    private HttpServletRequest request;
    private LineHandler lineHandler;


    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        lineHandler = LineHandler.getInstance();
    }

    @Test
    void testResetProtocol_Http() {
        when(request.getQueryString()).thenReturn("value=http://example.com");
        String result = lineHandler.resetProtocol(request);
        assertEquals("http%3A%2F%2Fexample.com", result);
    }

    @Test
    void testResetProtocol_Https() {
        when(request.getQueryString()).thenReturn("value=https://www.example.com");
        String result = lineHandler.resetProtocol(request);
        assertEquals("http%3A%2F%2Fexample.com", result);
    }

    @Test
    void testResetProtocol_Ftp() {
        when(request.getQueryString()).thenReturn("value=ftp://example.com");
        String result = lineHandler.resetProtocol(request);
        assertEquals("ftp%3A%2F%2Fexample.com", result);
    }
    @Test
    void testResetProtocol_NoProtocol() {
        when(request.getQueryString()).thenReturn("value=example.com");
        String result = lineHandler.resetProtocol(request);
        assertEquals("http%3A%2F%2Fexample.com", result);
    }
    @Test
    void testIsNotEmpty_NotEmpty() {
        when(request.getParameter("value")).thenReturn("someValue");
        assertTrue(lineHandler.isNotEmpty(request));
    }

    @Test
    void testIsNotEmpty_Empty() {
        when(request.getParameter("value")).thenReturn("");
        assertFalse(lineHandler.isNotEmpty(request));
    }

    @Test
    void testIsNotEmpty_Null() {
        when(request.getParameter("value")).thenReturn(null);
        assertFalse(lineHandler.isNotEmpty(request));
    }


    @Test
    void testIsLink_ValidLinkWithHttp() {
        when(request.getParameter("value")).thenReturn("http://example.com");
        assertTrue(lineHandler.isLink(request));
    }

    @Test
    void testIsLink_ValidLinkWithHttps() {
        when(request.getParameter("value")).thenReturn("https://example.com");
        assertTrue(lineHandler.isLink(request));
    }
    @Test
    void testIsLink_ValidLinkWithFtp() {
        when(request.getParameter("value")).thenReturn("ftp://example.com");
        assertTrue(lineHandler.isLink(request));
    }

    @Test
    void testIsLink_InvalidLink() {
        when(request.getParameter("value")).thenReturn("example");
        assertFalse(lineHandler.isLink(request));
    }

    @Test
    void testIsLink_InvalidLinkWithWww() {
        when(request.getParameter("value")).thenReturn("www.example");
        assertTrue(lineHandler.isLink(request));
    }

}