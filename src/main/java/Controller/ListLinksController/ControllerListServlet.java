package Controller.ListLinksController;


import Controller.ObserverForList;
import DAOLayer.ModelManager;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import java.io.IOException;

import java.io.PrintWriter;

@WebServlet(value = {"/home/controller/list"})
public class ControllerListServlet extends HttpServlet implements ObserverForList {
    ModelManager modelManager = ModelManager.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response){
        modelManager.handleListLinks(this, response, request, request.getRequestURL().toString().replace("/home/controller", "").replace("/list", ""));
    }

    @Override
    public void update(HttpServletResponse response, String userLogin, String links, int pageNumber, int totalPages) {
        PrintWriter out = null;
        try {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            out = response.getWriter();

            out.print(new JSONObject().put("result", userLogin).put("message", links).put("pageNumber", pageNumber).put("totalPages", totalPages).toString());

        } catch (NullPointerException | IOException | JSONException e){
            //*
            if(links.contains("messageForTestNullPointerException")){
                System.out.println("Ошибка при отправке ответа: " + "Test ControllerListServlet" + e.getMessage() + " ->(All OK)");
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
