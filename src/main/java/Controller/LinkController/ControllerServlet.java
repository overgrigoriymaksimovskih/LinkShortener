package Controller.LinkController;


import DAOLayer.ModelManager;

import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import java.io.PrintWriter;

@WebServlet(value = {"/home/controller"})
public class ControllerServlet extends HttpServlet implements Observer {

    ModelManager modelManager = ModelManager.getInstance();
    LineHandler lineHandler = LineHandler.getInstance();

    HttpSession session;
    @Override
    public void init() throws ServletException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        session = request.getSession();

        if (!lineHandler.isEmpty(request)){
            update(response,"Вы не ввели строку");
        } else if (!lineHandler.isLink(request)) {
            update(response,"Введенная строка не является ссылкой");
        } else {
            modelManager.handleLink(this, response, lineHandler.resetProtocol(request), request.getRequestURL().toString().replace("/home/controller", ""));
        }
    }
    @Override
    public void update(HttpServletResponse response, String result) {
        String addRes = "";
        try {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();

            if (session.getAttribute("isLogged")!= null && (Boolean) session.getAttribute("isLogged")) {
                addRes = " 1";
            } else {
                addRes = " 2";
            }
            System.out.println(session.getAttribute("isLogged"));
            out.println(new JSONObject().put("huy", result + addRes).toString());

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