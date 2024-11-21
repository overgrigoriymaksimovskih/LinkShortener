package View;

import DAOLayer.DbCreater;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet( value = {"/home"} )
public class ViewServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        DbCreater dbCreater = DbCreater.getInstance(this);
        dbCreater.createDB();

        if(dbCreater.isDBisExist()){
            response.setContentType("text/html; charset=UTF-8");
            String helloPage = "/WEB-INF/jsp/view.jsp";
            RequestDispatcher dispatcher = request.getRequestDispatcher(helloPage);
            dispatcher.include(request, response);
        }else{
            response.setContentType("text/html; charset=UTF-8");
            String helloPage = "/WEB-INF/jsp/viewError.jsp";
            RequestDispatcher dispatcher = request.getRequestDispatcher(helloPage);
            dispatcher.include(request, response);
        }
    }
}