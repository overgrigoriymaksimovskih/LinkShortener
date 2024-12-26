package Controller;

import javax.servlet.http.HttpServletResponse;

public interface ObserverForList {
    void update(HttpServletResponse response, String userLogin, String links, int pageNumber, int totalPages);
}
