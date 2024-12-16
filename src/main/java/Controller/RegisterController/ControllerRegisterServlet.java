package Controller.RegisterController;

import Controller.Observer;
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

@WebServlet(value = {"/home/controller/register"})
public class ControllerRegisterServlet extends HttpServlet implements Observer {
    ModelManager modelManager = ModelManager.getInstance();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        modelManager.handleRegister(this, response, request);
    }

    public void update(HttpServletResponse response, String result, String message) {
        try {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();

            out.println(new JSONObject().put("result", result).put("message", message).toString());

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
