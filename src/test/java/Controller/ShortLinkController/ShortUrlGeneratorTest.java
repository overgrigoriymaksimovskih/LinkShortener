package Controller.ShortLinkController;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class ShortUrlGeneratorTest {

    @Test
    void testGenerateShortUrlWithValidId() {
        long id = 12345;
        String shortUrl = ShortUrlGenerator.generateShortUrl(id);
        assertEquals(8, shortUrl.length());
        assertTrue(shortUrl.matches("[0-9a-f]+")); // Проверка, что строка содержит только hex-символы
    }

    @Test
    void testGenerateShortUrlWithDifferentIds() {
        long id1 = 12345;
        long id2 = 54321;
        String shortUrl1 = ShortUrlGenerator.generateShortUrl(id1);
        String shortUrl2 = ShortUrlGenerator.generateShortUrl(id2);
        assertNotEquals(shortUrl1, shortUrl2);
    }

    @Test
    void testGenerateShortUrlWithSameId() {
        long id = 12345;
        String shortUrl1 = ShortUrlGenerator.generateShortUrl(id);
        String shortUrl2 = ShortUrlGenerator.generateShortUrl(id);
        assertEquals(shortUrl1, shortUrl2);
    }

    @Test
    void testGenerateShortUrlWithLongId() {
        long id = 9223372036854775807L; // Max Long value
        String shortUrl = ShortUrlGenerator.generateShortUrl(id);
        assertEquals(8, shortUrl.length());
        assertTrue(shortUrl.matches("[0-9a-f]+")); // Проверка, что строка содержит только hex-символы
    }
}