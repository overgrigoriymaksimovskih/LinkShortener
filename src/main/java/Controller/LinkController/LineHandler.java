package Controller.LinkController;

import javax.servlet.http.HttpServletRequest;
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

    public String resetProtocol(HttpServletRequest request) {
        String someParam = request.getParameter("value");
        someParam.trim();
        if(someParam.contains("ftp://")){
            someParam = someParam.replaceAll("^ftp?://", ""); // удаляем http:// или https://
            return "ftp://" + someParam;
        }else{
            someParam = someParam.replaceAll("^https?://", ""); // удаляем http:// или https://
            someParam = someParam.replaceAll("^www\\.", ""); // удаляем www.
            return "http://" + someParam;
        }
    }
    public boolean isEmpty(HttpServletRequest request) {
        String someParam = request.getParameter("value");
        if (someParam == null || someParam.isEmpty()) {

            return false;
        }
        return true;
    }
    public boolean isLink(HttpServletRequest request) {
        String someParam = request.getParameter("value");
        Pattern pattern = Pattern.compile("^(https?:\\/\\/)?([\\p{L}\\d\\.-]+)\\.([\\p{L}\\.]{2,6})([\\/\\w\\.-]*)*\\/?$");
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

