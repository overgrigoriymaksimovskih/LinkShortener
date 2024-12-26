package Controller.LoginController;

import Controller.Observer;
import DAOLayer.ModelManager;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;

import java.io.PrintWriter;

@WebServlet(value = {"/home/controller/login"})
public class ControllerLoginServlet extends HttpServlet implements Observer {
    ModelManager modelManager = ModelManager.getInstance();
    HttpSession session;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response){
        String action = request.getParameter("action");
        if (action!= null && action.equals("logout")) {
            modelManager.handleLogout(this, request, response);
        } else {
            modelManager.handleLogin(this, response, request);
        }
    }

    @Override
    public void update(HttpServletResponse response, String result, String message) {
        PrintWriter out = null;
        try {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            out = response.getWriter();

            out.print(new JSONObject().put("result", result).put("message", message).toString());

        } catch (NullPointerException | IOException | JSONException e){
            //*
            if(message.contains("messageForTestNullPointerException")){
                System.out.println("Ошибка при отправке ответа: " + "Test ControllerLogin" + e.getMessage() + " ->(All OK)");
            }else{
                System.out.println("Ошибка при отправке ответа: " + e.getMessage());
            }
        } finally {
            try {
                if (out != null) {
                    response.getWriter().close();
                }
            } catch (IOException e) {
                //*
                System.err.println("Ошибка при закрытии потока вывода: " + e.getMessage());
            }
        }
    }
}