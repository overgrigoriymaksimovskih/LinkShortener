package View;

import DAOLayer.DbCreater;
import DAOLayer.ModelManager;

import javax.servlet.annotation.WebServlet;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet( value = {"/home"} )
public class ViewServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        DbCreater dbCreater = DbCreater.getInstance(this);
        dbCreater.createDB();

        ModelManager modelManager = ModelManager.getInstance();

        if(dbCreater.isDBisExist() && modelManager.isLogin(request)){
            response.setContentType("text/html; charset=UTF-8");
            String helloPage = "/WEB-INF/jsp/viewAuth.jsp";
            RequestDispatcher dispatcher = request.getRequestDispatcher(helloPage);
            dispatcher.include(request, response);
            return;
        }else if(dbCreater.isDBisExist() && !modelManager.isLogin(request)){
            response.setContentType("text/html; charset=UTF-8");
            String helloPage = "/WEB-INF/jsp/viewNotAuth.jsp";
            RequestDispatcher dispatcher = request.getRequestDispatcher(helloPage);
            dispatcher.include(request, response);
            return;
        }
        response.setContentType("text/html; charset=UTF-8");
        String helloPage = "/WEB-INF/jsp/viewError.jsp";
        RequestDispatcher dispatcher = request.getRequestDispatcher(helloPage);
        dispatcher.include(request, response);
    }
}