package DAOLayer;

import Controller.DeleteController.ControllerDeleteAccServlet;
import Controller.LinkController.ControllerServlet;
import Controller.ListLinksController.ControllerListServlet;
import Controller.LoginController.ControllerLoginServlet;
import Controller.RegisterController.ControllerRegisterServlet;
import Controller.ShortLinkController.ShortUrlGenerator;

import DAOLayer.Entity.Url;
import DAOLayer.Entity.User;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.NoResultException;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;

public class ModelManager {
    // не будем здесь реализовывать часть связанную с интерфейсом обзервер, потому что модель менеджер общий и
    // единственный для всех контроллеров и значит в лист будет бесконечно расти для каждого нового контроллера
    // можно через WeakHashMap сделать но сложно, а так он и сам знает кому отдавать потому что response тот же что и
    // изначально

    private static volatile ModelManager instance;
    private ModelManager() {}

    public static ModelManager getInstance() {
        if (instance == null) {
            synchronized (ModelManager.class) {
                if (instance == null) {
                    instance = new ModelManager();
                }
            }
        }
        return instance;
    }

    public void handleLink(ControllerServlet controllerServlet, HttpServletResponse response, String link, String host, HttpServletRequest request) {

        System.out.println("dffsd " + link);
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("userId");

        try (Session hibernateSession = HibernateUtil.getSessionFactory().getCurrentSession()) {
            hibernateSession.beginTransaction();

            Long count = (Long) hibernateSession.createQuery("SELECT COUNT(*) FROM Url WHERE originalUrl = :originalUrl")
                    .setParameter("originalUrl", link)
                    .getSingleResult();

            if (count > 0) {
                // Если короткая ссылка уже существует, использовать ее
                Url existingUrl = (Url) hibernateSession.createQuery("FROM Url WHERE originalUrl = :originalUrl")
                        .setParameter("originalUrl", link)
                        .getSingleResult();

                // Проверяем, принадлежит ли ссылка зарегистрированному пользователю
                if (existingUrl.getUserId()!= null && existingUrl.getUserId() > 0) {
                    // Ссылка принадлежит зарегистрированному пользователю, используем ее
                    String shortUrl = existingUrl.getShortUrl();

                    //Делаем человекопонятный вид. Отправляем в вьюху
                    controllerServlet.update(response, "success", host + "/s/" + shortUrl);
                } else {
                    // Ссылка не принадлежит зарегистрированному пользователю, изменяем владельца
                    existingUrl.setUserId(userId);
                    hibernateSession.update(existingUrl);

                    //Делаем человекопонятный вид. Отправляем в вьюху
                    controllerServlet.update(response, "success", host + "/s/" + existingUrl.getShortUrl());
                }
            } else {
                // Если короткая ссылка не существует, создать новую
                Url url = new Url();
                url.setOriginalUrl(link);
                url.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                url.setUserId(userId);
                hibernateSession.save(url);

                // Получить значение столбца AUTO_INCREMENT
                long id = url.getId();

                // Преобразовать значение столбца AUTO_INCREMENT в сокращенный URL
                String shortUrl = ShortUrlGenerator.generateShortUrl(id);

                // Обновить запись в таблице с сокращенным URL
                url.setShortUrl(shortUrl);
                hibernateSession.update(url);

                //Делаем человекопонятный вид. Отправляем в вьюху
                System.out.println(host + "/s/" + shortUrl);
                controllerServlet.update(response, "success", host + "/s/" + shortUrl);
            }

            hibernateSession.getTransaction().commit();

        } catch (Exception e) {
            System.out.println("Ошибко при обработке запроса: " + e.getMessage());
            controllerServlet.update(response, "fail", "ошибка при работе с базой данных");
            return;
        }
    }

    public void handleLogin(ControllerLoginServlet loginControllerServlet, HttpServletResponse response, HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session.getAttribute("isLogged")!= null && (Boolean) session.getAttribute("isLogged")) {
            loginControllerServlet.update(response, "fail", "Вы уже авторизованы");
            return;
        }

        String login = request.getParameter("login");
        String password = request.getParameter("password");

        try (Session hibernateSession = HibernateUtil.getSessionFactory().getCurrentSession()) {
            hibernateSession.beginTransaction();

            Query<User> query = hibernateSession.createQuery("FROM User WHERE login = :login", User.class);
            query.setParameter("login", login);

            User user = query.uniqueResult();

            if (user!= null && user.getPassword().equals(password)) {
                session.setAttribute("isLogged", true);
                session.setAttribute("userId", user.getId());
//                session.setAttribute("userLogin", user.getLogin());
                loginControllerServlet.update(response, "success", "Бобро пожаловать" + user.getLogin());
            } else if (user == null) {
                loginControllerServlet.update(response, "fail", "Пользователь с таким логином не найден");
            } else {
                loginControllerServlet.update(response, "fail", "Неверный пароль");
            }

            hibernateSession.getTransaction().commit();
        } catch (Exception e) {
            System.out.println("Ошибка при обработке запроса: " + e.getMessage());
            loginControllerServlet.update(response, "fail", "Ошибка при работе с базой данных");
            return;
        }
    }

    public void handleLogout(ControllerLoginServlet loginControllerServlet, HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        session.removeAttribute("isLogged");
        session.removeAttribute("userId");
        loginControllerServlet.update(response, "success", "Выход выполнен успешно");
    }

    public void handleRegister(ControllerRegisterServlet registerControllerServlet, HttpServletResponse response, HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session.getAttribute("isLogged")!= null && (Boolean) session.getAttribute("isLogged")) {
            registerControllerServlet.update(response, "fail", "Вы уже авторизованы");
            return;
        }

        String login = request.getParameter("login");
        String password = request.getParameter("password");

        // Проверка на не пустое поле логина
        if (login == null || login.isEmpty()) {
            registerControllerServlet.update(response, "fail", "Логин не может быть пустым");
            return;
        }

        // Проверка на длину логина (минимум 3 символа, максимум 20 символов)
        if (login.length() < 3 || login.length() > 20) {
            registerControllerServlet.update(response, "fail", "Логин должен быть не менее 3 символов и не более 20 символов");
            return;
        }

        // Проверка на не пустое поле пароля
        if (password == null || password.isEmpty()) {
            registerControllerServlet.update(response, "fail", "Пароль не может быть пустым");
            return;
        }

        // Проверка на длину пароля (минимум 8 символов, максимум 50 символов)
        if (password.length() < 8 || password.length() > 50) {
            registerControllerServlet.update(response, "fail", "Пароль должен быть не менее 8 символов и не более 50 символов");
            return;
        }

        try (Session hibernateSession = HibernateUtil.getSessionFactory().getCurrentSession()) {
            hibernateSession.beginTransaction();

            Query<User> query = hibernateSession.createQuery("FROM User WHERE login = :login", User.class);
            query.setParameter("login", login);

            User user = query.uniqueResult();

            if (user!= null) {
                registerControllerServlet.update(response, "fail", "Пользователь с таким логином уже существует");
            } else {
                User newUser = new User();
                newUser.setLogin(login);
                newUser.setPassword(password);
                hibernateSession.save(newUser);

                registerControllerServlet.update(response, "success", "Регистрация успешна");
            }

            hibernateSession.getTransaction().commit();
        } catch (Exception e) {
            System.out.println("Ошибка при обработке запроса: " + e.getMessage());
            registerControllerServlet.update(response, "fail", "Ошибка при работе с базой данных");
            return;
        }
    }

    public void handleDelete(ControllerDeleteAccServlet controllerServlet, HttpServletResponse response, HttpServletRequest request) {
        HttpSession session = request.getSession();
        Boolean isLogin = (Boolean) session.getAttribute("isLogged");
        Long userId = (Long) session.getAttribute("userId");

        if (isLogin!= null && isLogin) {
            try (Session hibernateSession = HibernateUtil.getSessionFactory().getCurrentSession()) {
                hibernateSession.beginTransaction();

                User user = hibernateSession.find(User.class, userId);

                if (user!= null) {
                    // Удалить пользователей
                    hibernateSession.remove(user);

                    // Обновить записи в таблице Url
                    List<Url> urls = hibernateSession.createQuery("FROM Url WHERE userId = :userId", Url.class)
                            .setParameter("userId", userId)
                            .getResultList();

                    for (Url url : urls) {
                        url.setUserId(null);
                        hibernateSession.update(url);
                    }

                    hibernateSession.getTransaction().commit();
                    session.removeAttribute("isLogged");
                    session.removeAttribute("userId");
                    controllerServlet.update(response, "success", "Пользователь успешно удален");
                } else {
                    controllerServlet.update(response, "fail", "Пользователь не найден");
                }

            } catch (Exception e) {
                System.out.println("Ошибка при обработке запроса: " + e.getMessage());
                controllerServlet.update(response, "fail", "Ошибка при работе с базой данных");
                return;
            }
        } else {
            controllerServlet.update(response, "fail", "Вы не авторизованы");
        }
    }

    public void handleListLinks(ControllerListServlet listControllerServlet, HttpServletResponse response, HttpServletRequest request, String host) {
        HttpSession session = request.getSession();
        Boolean isLogin = (Boolean) session.getAttribute("isLogged");
        Long userId = (Long) session.getAttribute("userId");

        if (isLogin!= null && isLogin) {
            try (Session hibernateSession = HibernateUtil.getSessionFactory().getCurrentSession()) {
                hibernateSession.beginTransaction();

                Query<User> query = hibernateSession.createQuery("FROM User WHERE id = :userId", User.class);
                query.setParameter("userId", userId);

                User user = query.uniqueResult();

                if (user!= null) {
                    String login = user.getLogin();

                    Query<Url> urlQuery = hibernateSession.createQuery("FROM Url WHERE userId = :userId ORDER BY createdAt DESC", Url.class);
                    urlQuery.setParameter("userId", userId);

                    int pageSize = 7; // количество записей на странице
                    int pageNumber = request.getParameter("pageNumber")!= null? Integer.parseInt(request.getParameter("pageNumber")) : 1; // номер страницы

                    List<Url> urls = urlQuery.getResultList();
//                    Collections.reverse(urls);

                    int start = (pageNumber - 1) * pageSize;
                    int end = start + pageSize;

                    List<Url> paginatedUrls = urls.subList(start, Math.min(end, urls.size()));

                    LinkedHashMap<String, String> json = new LinkedHashMap<>();
                    for (Url a : paginatedUrls){
                        String originalUrl = a.getOriginalUrl();
                        try {
                            originalUrl = URLDecoder.decode(originalUrl, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            System.out.println("Ошибка при декодировании ссылки: " + e.getMessage());
                        }
                        json.put(host + "/s/" + a.getShortUrl(), originalUrl);
                        System.out.println(originalUrl);
                    }
                    ObjectMapper mapper = new ObjectMapper();
                    String jsonObject = mapper.writeValueAsString(json);

                    Query<Long> countQuery = hibernateSession.createQuery("SELECT COUNT(*) FROM Url WHERE userId = :userId", Long.class);
                    countQuery.setParameter("userId", userId);
                    long totalRecords = countQuery.uniqueResult();

                    int totalPages = (int) Math.ceil((double) totalRecords / pageSize);

                    listControllerServlet.update(response, login, jsonObject.toString(), pageNumber, totalPages);

                } else {
                    listControllerServlet.update(response, "fail", "Пользователь не найден", 1, 1);
                }

                hibernateSession.getTransaction().commit();
            } catch (Exception e) {
                System.out.println("Ошибка при обработке запроса: " + e.getMessage());
                listControllerServlet.update(response, "fail", "Ошибка при работе с базой данных", 1, 1);
                return;
            }
        } else {
            listControllerServlet.update(response, "fail", "Вы не авторизованы", 1, 1);
        }
    }

    public String getOriginalLink(String shortLink) {
        try (Session session = HibernateUtil.getSessionFactory().getCurrentSession()) {
            session.beginTransaction();

            Url existingUrl = (Url) session.createQuery("FROM Url WHERE shortUrl = :shortUrl", Url.class)
                    .setParameter("shortUrl", shortLink)
                    .getSingleResult();

            session.getTransaction().commit();

            try {
                return URLDecoder.decode(existingUrl.getOriginalUrl(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                System.out.println("Ошибка при декодировании ссылки: " + e.getMessage());
                return null;
            }
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            System.out.println("Ошибка при обработке запроса: " + e.getMessage());
            return null;
        }
    }

    public boolean isLogin(HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session.getAttribute("isLogged")!= null && (Boolean) session.getAttribute("isLogged")) {
            return true;
        }else{
            return false;
        }
    }
}