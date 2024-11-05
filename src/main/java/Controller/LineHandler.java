package Controller;

import View.View;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LineHandler {
    public static boolean checkLine (String str){
        if (isUrl(resetProtocol(str))) {
            return true;
        }else if (isFtp(resetProtocol(str))){
            return true;
        }else{
            return false;
        }
    }
    public static String handleLine(String str, View view){
        String handledLine = str;
        if (isUrl(resetProtocol(handledLine))) {
            view.printHandleUrl();
        }else if (isFtp(resetProtocol(handledLine))){
            view.printHandleFtp();
        }else{
            view.printNotUrl();
        }
        return handledLine;
    }
    //------------------------------------------------------------------------------------------------------------------
    private static String resetProtocol(String str) {
        if(str.contains("ftp://")){
            return str;
        }else{
            str = str.replaceAll("^https?://", ""); // удаляем http:// или https://
            str = str.replaceAll("^www\\.", ""); // удаляем www.
            return "http://" + str;
        }
    }
    private static boolean isUrl(String str) {
        Pattern pattern = Pattern.compile("^(https?:\\/\\/)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([\\/\\w\\.-]*)*\\/?$");
        Matcher matcher = pattern.matcher(str);
        if (matcher.matches()) {
            return true;
        } else {
            return false;
        }
    }
    private static boolean isFtp(String str) {
        if (str.startsWith("ftp://")) {
            return true;
        } else {
            return false;
        }
    }
}
