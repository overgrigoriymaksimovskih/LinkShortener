package Model.Servlet;


import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet( value = {"/unnamed"} )
public class Servlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        // Получаем параметр “secret” из запроса
//        String secret = request.getParameter("secret");
//
//        // Кладем параметр “secret” в Http-сессию
//        HttpSession session = request.getSession(true);
//        session.setAttribute("secret", secret);

        // Печатаем HTML в качестве ответа для браузера
//
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("value", 57575757);
            out.println(jsonObject.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } finally {
            out.close();
        }
    }
}
