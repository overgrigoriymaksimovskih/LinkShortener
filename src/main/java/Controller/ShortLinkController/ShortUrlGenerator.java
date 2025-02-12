package Controller.ShortLinkController;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ShortUrlGenerator {
    public static String generateShortUrl(long id){
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            //*
            throw new RuntimeException(e);
        }
        byte[] hashBytes = md.digest(String.valueOf(id).getBytes());
        String shortUrl = bytesToHex(hashBytes).substring(0, 8);
        return shortUrl;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}