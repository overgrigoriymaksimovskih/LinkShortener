package Controller.LoginController;

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

@WebServlet(value = {"/home/controller/login"})
public class ControllerLoginServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {

        // Создаем сессию
        HttpSession session = req.getSession();
        if (AccessHandler.checkLogout(req)){
            session.removeAttribute("isLogged");
        }else if (AccessHandler.checkLoginPassword(req)){
            // Храним информацию о пользователе в сессии
            session.setAttribute("isLogged", true);
        }

        try {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            out.println(new JSONObject().put("result", AccessHandler.result).put("message", AccessHandler.message).toString());
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