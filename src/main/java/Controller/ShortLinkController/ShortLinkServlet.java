package Controller.ShortLinkController;

import DAOLayer.DbCreator;
import DAOLayer.ModelManager;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

    @WebServlet(name = "ShortLinkServlet", urlPatterns = "/s/*")
    public class ShortLinkServlet extends HttpServlet {
        ModelManager modelManager = ModelManager.getInstance();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp){
        String shortLink = req.getRequestURI().substring(req.getContextPath().length() + req.getServletPath().length() + 1);
        String originalLink = modelManager.getOriginalLink(shortLink);

        if (originalLink!= null) {
            try {
                resp.sendRedirect(originalLink);
            } catch (IOException e) {
                //*
                throw new RuntimeException(e);
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
