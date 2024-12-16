package Controller.ListLinksController;

import DAOLayer.ModelManager;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(value = {"/home/controller/list"})
public class ControllerListServlet extends HttpServlet {
    ModelManager modelManager = ModelManager.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        modelManager.handleListLinks(this, response, request, request.getRequestURL().toString().replace("/home/controller", "").replace("/list", ""));
    }

//    public void update(HttpServletResponse response, String userLogin, String links) {
//        try {
//            response.setContentType("application/json");
//            response.setCharacterEncoding("UTF-8");
//            PrintWriter out = response.getWriter();
//
//            out.println(new JSONObject().put("result", userLogin).put("message", links).toString());
//
//        } catch (IOException | JSONException e) {
//            System.out.println("Ошибка при отправке ответа: " + e.getMessage());
//        } finally {
//            try {
//                if (response.getWriter()!= null) {
//                    response.getWriter().close();
//                }
//            } catch (IOException e) {
//                System.err.println("Ошибка при закрытии потока вывода: " + e.getMessage());
//            }
//        }
//    }
public void update(HttpServletResponse response, String userLogin, String links, int pageNumber, int totalPages) {
    try {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        out.println(new JSONObject().put("result", userLogin).put("message", links).put("pageNumber", pageNumber).put("totalPages", totalPages).toString());

    } catch (IOException | JSONException e) {
        System.out.println("Ошибка при отправке ответа: " + e.getMessage());
    } finally {
        try {
            if (response.getWriter()!= null) {
                response.getWriter().close();
            }
        } catch (IOException e) {
            System.err.println("Ошибка при закрытии потока вывода: " + e.getMessage());
        }
    }
}
}
