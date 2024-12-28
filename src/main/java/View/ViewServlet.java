package View;

import DAOLayer.DbCreator;
import DAOLayer.ModelManager;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(value = {"/home"})
public class ViewServlet extends HttpServlet {

    private DbCreator dbCreator;
    private final ModelManager modelManager;

    public ViewServlet(DbCreator dbCreator, ModelManager modelManager) {
        this.dbCreator = dbCreator;
        this.modelManager = modelManager;
    }

    public ViewServlet() {
        this(null, ModelManager.getInstance());
    }

    @Override
    public void init() throws ServletException {
        super.init();
        ServletContext servletContext = getServletContext();
        if (servletContext != null) {
            dbCreator = (DbCreator) servletContext.getAttribute("dbCreator");
        }
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(dbCreator!=null) {
            handleRequest(request, response);
        }else{
            sendErrorPage(request,response);
        }
    }

    private void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setContentType(response);
        if (modelManager.isLogin(request)) {
            sendAuthPage(request, response);
        } else {
            sendNotAuthPage(request, response);
        }
    }

    private void setContentType(HttpServletResponse response) {
        response.setContentType("text/html; charset=UTF-8");
    }

    private void sendAuthPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/viewAuth.jsp");
            dispatcher.include(request, response);
        } catch (ServletException e) {
            e.printStackTrace();
        }
    }

    private void sendNotAuthPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/viewNotAuth.jsp");
            dispatcher.include(request, response);
        } catch (ServletException e) {
            e.printStackTrace();
        }
    }

    private void sendErrorPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/viewError.jsp");
            dispatcher.include(request, response);
        } catch (ServletException e) {
            e.printStackTrace();
        }
    }
}