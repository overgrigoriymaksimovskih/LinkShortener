package Controller;

import javax.servlet.http.HttpServletResponse;

public interface Observer {
    void update(HttpServletResponse response, String result, String message);
}
