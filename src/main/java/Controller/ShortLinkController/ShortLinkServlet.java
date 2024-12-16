package Controller.ShortLinkController;

import DAOLayer.ModelManager;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

    @WebServlet(name = "ShortLinkServlet", urlPatterns = "/s/*") public class ShortLinkServlet extends HttpServlet {
        ModelManager modelManager = ModelManager.getInstance();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String shortLink = req.getRequestURI().substring(req.getContextPath().length() + req.getServletPath().length() + 1);
        String originalLink = modelManager.getOriginalLink(shortLink);

        if (originalLink!= null) {
            resp.sendRedirect(originalLink);
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
