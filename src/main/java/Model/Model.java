package Model;

import Controller.Controller;
import Model.RequestsFromIDE.Client;
import Model.RequestsFromIDE.FactoryOfRequests;
import Model.RequestsFromIDE.RequestTypes;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class Model {
    private Controller controller;
    List<String> list = new ArrayList<String>();

    public Model(Controller controller) {
        this.controller = controller;
    }
    public boolean initialisation(){
        return true;
    }
    public void getDomain(String domain) throws InterruptedException{

        HttpClient client = new Client().getClient();
        HttpRequest request = FactoryOfRequests.createRequest(RequestTypes.GETOBJECTID);
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            // Проверяем статус ответа
            if (response.statusCode() == 200) {
                // Парсим JSON-строку в объект JSON
                String responseBody = response.body();
                JSONObject jsonObject = new JSONObject(responseBody);
                int value = jsonObject.getInt("value");
                controller.update(value);
            } else {
                System.out.println("Error: " + response.statusCode());
            }
        }catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
