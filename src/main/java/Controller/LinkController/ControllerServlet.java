package Controller.LinkController;


import Controller.Observer;
import DAOLayer.DbCreator;
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
import org.slf4j.*;

@WebServlet(value = {"/home/controller"})
public class ControllerServlet extends HttpServlet implements Observer {
    private static final Logger log = LoggerFactory.getLogger(DbCreator.class);
    ModelManager modelManager = ModelManager.getInstance();
    LineHandler lineHandler = LineHandler.getInstance();
    HttpSession session;
    @Override
    public void init() throws ServletException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response){

        session = request.getSession();

        if (!lineHandler.isNotEmpty(request)){
            update(response, "fail", "Вы не ввели строку");
        } else if (!lineHandler.isLink(request)) {
            update(response, "fail", "Введенная строка не является ссылкой");
        } else {
            modelManager.handleLink(this, response, lineHandler.resetProtocol(request), request.getRequestURL().toString().replace("/home/controller", ""), request);
        }
    }

    @Override
    public void update(HttpServletResponse response, String result,  String message) {
        PrintWriter out = null;
        try {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            out = response.getWriter();

            out.print(new JSONObject().put("huy", message).toString());

        } catch (NullPointerException | IOException | JSONException e){
            //*
            if(message.contains("Test exception occurred")){
//                System.out.println("Ошибка при отправке ответа: " + "Test ControllerServlet" + e.getMessage() + " ->(All OK)");
                log.debug("Ошибка при отправке ответа: " + "Test ControllerServlet" + e.getMessage() + " ->(All OK)");
            }else{
//                System.out.println("Ошибка при отправке ответа: " + e.getMessage());
                log.warn("Ошибка при отправке ответа: " + e.getMessage());
            }
        } finally {
            try {
                if (out != null) {
                    response.getWriter().close();
                }
            } catch (IOException e) {
                if(e.getMessage().contains("All OK")){
                    log.debug("Ошибка при закрытии потока вывода: " + "Test ControllerDeleteServlet " + e.getMessage() + " ->(All OK)");
                }else{
                    log.warn("Ошибка при закрытии потока вывода: " + e.getMessage());
                }
            }
        }
    }
}