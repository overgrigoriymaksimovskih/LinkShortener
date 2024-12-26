package Controller.LinkController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LineHandler {
    private static volatile LineHandler instance;

    private LineHandler() {}

    public static LineHandler getInstance() {
        if (instance == null) {
            synchronized (LineHandler.class) {
                if (instance == null) {
                    instance = new LineHandler();
                }
            }
        }
        return instance;
    }


//    public String resetProtocol(HttpServletRequest request) {
//        String someParam = request.getQueryString();
//        someParam = someParam.replace("value=", "");
//        if(someParam.contains("ftp://")){
//            someParam = someParam.replaceAll("^ftp?://", ""); // удаляем http:// или https://
//            someParam =  "ftp://" + someParam;
//        }else{
//            someParam = someParam.replaceAll("^https?://", ""); // удаляем http:// или https://
//            someParam = someParam.replaceAll("^www\\.", ""); // удаляем www.
//            someParam =  "http://" + someParam;
//        }
//        someParam = UrlValidator.getInstance().isValid(someParam)? someParam : "";
//        try {
//            someParam = URLEncoder.encode(someParam, "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            throw new RuntimeException(e);
//        }
//        return someParam;
//    }


    String resetProtocol(HttpServletRequest request) {
        String someParam = request.getQueryString();
        someParam = someParam.replace("value=", "");
        if(someParam.contains("ftp://")){
            someParam = someParam.replaceAll("^ftp?://", ""); // удаляем http:// или https://
            someParam =  "ftp://" + someParam;
        }else{
            someParam = someParam.replaceAll("^https?://", ""); // удаляем http:// или https://
            someParam = someParam.replaceAll("^www\\.", ""); // удаляем www.
            someParam =  "http://" + someParam;
        }
        try {
            someParam = URLEncoder.encode(someParam, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return someParam;
    }


    public boolean isNotEmpty(HttpServletRequest request) {
        String someParam = request.getParameter("value");
        if (someParam == null || someParam.isEmpty()) {

            return false;
        }
        return true;
    }
    public boolean isLink(HttpServletRequest request) {
        String someParam = request.getParameter("value");
        Pattern pattern = Pattern.compile("^[^\\.]*\\..{2,}.*");
        Matcher matcher = pattern.matcher(someParam);
        if (matcher.matches()) {
            return true;
        } else {
            if (someParam.startsWith("ftp://")) {
                return true;
            } else {
                return false;
            }
        }
    }
}

