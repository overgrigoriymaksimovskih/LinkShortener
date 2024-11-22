package Controller.LoginController;

import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;

final class AccessHandler {
    private AccessHandler() {
    }

    static String result;
    static String message;

    static boolean checkLoginPassword(HttpServletRequest req) throws IOException {
//        // Читаем данные из тела запроса в формате JSON
//        BufferedReader reader = req.getReader();
//        StringBuilder json = new StringBuilder();
//        String line;
//        while ((line = reader.readLine()) != null) {
//            json.append(line);
//        }
//
//        // Парсим JSON
//        JSONObject jsonObject = null;
//        try {
//            jsonObject = new JSONObject(json.toString());
//        } catch (JSONException e) {
//            throw new RuntimeException(e);
//        }
//
//        // Получаем значения логина и пароля из JSON
//        String login = null;
//        try {
//            login = jsonObject.getString("login");
//        } catch (JSONException e) {
//            throw new RuntimeException(e);
//        }
//        String password = null;
//        try {
//            password = jsonObject.getString("password");
//            System.out.println(password);
//        } catch (JSONException e) {
//            throw new RuntimeException(e);
//        }
//
//
//        // Обрабатываем логин и пароль
//        if (login != null && password != null) {
//            // Проверяем логин и пароль
//            if (login.equals("user") && password.equals("password")) {
//                // Если логин и пароль верны, отправляем ответ
//                result = "success";
//                message = "Бобро пожаловать";
//                return true;
//            } else if (!login.equals("user")) {
//                // Если логин неверны, отправляем ошибку
//                result = "fail";
//                message = "Пользователь с таким логином не найден";
//                return false;
//            } else if (!password.equals("password")) {
//                // Если пароль неверны, отправляем ошибку
//                result = "fail";
//                message = "Неверный пароль";
//                return false;
//            } else {
//                result = "fail";
//                message = "Не возможно обработать данные";
//                return false;
//            }
//        } else {
//            // Если логин или пароль не переданы, отправляем ошибку
//            result = "fail";
//            message = "Не возможно обработать данные";
//            return false;
//        }
        String login = req.getParameter("login");
        String password = req.getParameter("password");

        // Обрабатываем логин и пароль
        if (login!= null && password!= null) {
            // Проверяем логин и пароль
            if (login.equals("user") && password.equals("password")) {
                // Если логин и пароль верны, отправляем ответ
                result = "success";
                message = "Бобро пожаловать";
                return true;
            } else if (!login.equals("user")) {
                // Если логин неверны, отправляем ошибку
                result = "fail";
                message = "Пользователь с таким логином не найден";
                return false;
            } else if (!password.equals("password")) {
                // Если пароль неверны, отправляем ошибку
                result = "fail";
                message = "Неверный пароль";
                return false;
            } else {
                result = "fail";
                message = "Не возможно обработать данные";
                return false;
            }
        } else {
            // Если логин или пароль не переданы, отправляем ошибку
            result = "fail";
            message = "Не возможно обработать данные";
            return false;
        }
    }
    static boolean checkLogout(HttpServletRequest req) throws IOException{
//        // Читаем данные из тела запроса в формате JSON
//        BufferedReader reader = req.getReader();
//        StringBuilder json = new StringBuilder();
//        String line;
//        while ((line = reader.readLine()) != null) {
//            json.append(line);
//        }
//
//        // Парсим JSON
//        JSONObject jsonObject = null;
//        try {
//            jsonObject = new JSONObject(json.toString());
//        } catch (JSONException e) {
//            throw new RuntimeException(e);
//        }
//
//        // Получаем значения логина и пароля из JSON
//        String action = null;
//        try {
//            action = jsonObject.getString("action");
//        } catch (JSONException e) {
//            throw new RuntimeException(e);
//        }
//
//
//
//        // Обрабатываем action
//        if (action != null) {
//            // Проверяем логин и пароль
//            if (action.equals("logout")) {
//                return true;
//            } else {
//                return false;
//            }
//
//        }else{
//            return false;
//        }
        //----
        String action = req.getParameter("action");
        if (action!= null && action.equals("logout")) {
            return true;
        } else {
            return false;
        }
    }
}