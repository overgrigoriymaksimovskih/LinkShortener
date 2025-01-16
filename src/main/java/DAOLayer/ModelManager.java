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
import jakarta.persistence.TypedQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

public class ModelManager {
    // не будем здесь реализовывать часть связанную с интерфейсом обзервер, потому что модель менеджер общий и
    // единственный для всех контроллеров и значит в лист будет бесконечно расти для каждого нового контроллера
    // можно через WeakHashMap сделать но сложно, а так он и сам знает кому отдавать потому что response тот же что и
    // изначально

    private static final Logger log = LoggerFactory.getLogger(ModelManager.class);
//    private static final Logger log = LoggerFactory.getLogger(DbCreator.class);
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
//----------------------------------------------------------------------------------------------------------------------
    public void handleLink(ControllerServlet controllerServlet, HttpServletResponse response, String link, String host, HttpServletRequest request) {
    HttpSession session = request.getSession();
    log.trace("handleLink: Session state: isLogged = {}, userId = {}", session.getAttribute("isLogged"), session.getAttribute("userId"));
    Long userId = (Long) session.getAttribute("userId");
    Transaction tx = null;

        try (Session hibernateSession = HibernateUtil.getSessionFactory().openSession()) {
            tx = hibernateSession.beginTransaction();
            log.trace("Starting to process link shortening request for user: " + userId + ", link: " + link);

            Optional<Url> existingUrl = findUrlByOriginalUrl(hibernateSession, link, userId);
            log.trace("findUrlByOriginalUrl result: " + (existingUrl.isPresent() ? "URL found" : "URL not found"));
            if (existingUrl.isPresent()) {
                String shortUrl = existingUrl.get().getShortUrl();
                controllerServlet.update(response, "success", host + "/s/" + shortUrl);
                log.trace("user: " + userId + " input link: " + link + " and get existing shortUrl: " + shortUrl);
            } else {
                Url newUrl = new Url();
                newUrl.setOriginalUrl(link);
                newUrl.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                newUrl.setUserId(userId); //

                long id = newUrl.getId(); // Get the ID after persist
                String shortUrl = ShortUrlGenerator.generateShortUrl(id);
                newUrl.setShortUrl(shortUrl);

                log.trace("Generated shortUrl: " + shortUrl);
                hibernateSession.persist(newUrl);
                tx.commit();
                tx = hibernateSession.beginTransaction();
                log.trace("Persisted new URL with ID: " + newUrl.getId() + ", shortUrl: " + shortUrl);

                hibernateSession.merge(newUrl);
                controllerServlet.update(response, "success", host + "/s/" + shortUrl);
                log.trace("user: " + userId + " input link: " + link + " and get new shortUrl: " + shortUrl);
            }

            tx.commit();
            log.trace("user: " + userId + " commit link " + link + " successful");
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            log.error("Error processing request, userId: " + userId + ", link: " + link + ", message: " + e.getMessage(), e);
            log.error("Error processing request: " + e.toString());
            controllerServlet.update(response, "fail", "Database error");
        }
}

    private Optional<Url> findUrlByOriginalUrl(Session session, String originalUrl, Long userId) {
        String hql = "FROM Url WHERE originalUrl = :originalUrl AND userId = :userId";
        log.trace("findUrlByOriginalUrl called with originalUrl: {}, userId: {}", originalUrl, userId);
        log.trace("HQL Query: {}", hql);
        try {
            TypedQuery<Url> query = session.createQuery(hql, Url.class);
            query.setParameter("originalUrl", originalUrl);
            query.setParameter("userId", userId);
            Url result = query.getSingleResult();
            log.trace("findUrlByOriginalUrl: URL found with id {}", result.getId());
            return Optional.ofNullable(query.getSingleResult());
        } catch (NoResultException e) {
            log.trace("findUrlByOriginalUrl: URL not found for originalUrl: {}, userId: {}", originalUrl, userId);
            return Optional.empty();
        }
    }
//----------------------------------------------------------------------------------------------------------------------
    public void handleLogin(ControllerLoginServlet loginControllerServlet, HttpServletResponse response, HttpServletRequest request) {
        log.trace("handleLogin started");
        HttpSession session = request.getSession();
        log.trace("Session state isLogged: {}", session.getAttribute("isLogged"));
        if (session.getAttribute("isLogged")!= null && (Boolean) session.getAttribute("isLogged")) {
            loginControllerServlet.update(response, "fail", "Вы уже авторизованы");
            log.trace("User is already logged in.");
            return;
        }
        String login = request.getParameter("login");
        String password = request.getParameter("password");
        try (Session hibernateSession = HibernateUtil.getSessionFactory().getCurrentSession()) {
            hibernateSession.beginTransaction();
            String hql = "FROM User WHERE login = :login";
            log.trace("HQL query: {}", hql);
            Query<User> query = hibernateSession.createQuery("FROM User WHERE login = :login", User.class);
            query.setParameter("login", login);

            User user = query.uniqueResult();
            log.trace("Query result: user {}", user != null ? user.getId() + ", " + user.getLogin() : "not found");

            if (user!= null && user.getPassword().equals(password)) {
                log.trace("Password match for user: {}", user.getId());
                session.setAttribute("isLogged", true);
                session.setAttribute("userId", user.getId());
//                session.setAttribute("userLogin", user.getLogin());
                log.trace("Session attributes set: isLogged = true, userId = {}", user.getId());
                loginControllerServlet.update(response, "success", "Бобро пожаловать" + user.getLogin());
                log.trace("Sending success message to controller.");
            } else if (user == null) {
                loginControllerServlet.update(response, "fail", "Пользователь с таким логином не найден");
            } else {
                log.trace("Incorrect password for user {}", user.getId());
                loginControllerServlet.update(response, "fail", "Неверный пароль");
            }

            hibernateSession.getTransaction().commit();
            log.trace("Transaction commited.");
        } catch (Exception e) {
            log.error("Error during login, message: " + e.getMessage(), e);
//            System.out.println("Ошибка при обработке запроса: " + e.getMessage());
            loginControllerServlet.update(response, "fail", "Ошибка при работе с базой данных");
            return;
        }
    }
//----------------------------------------------------------------------------------------------------------------------
    public void handleLogout(ControllerLoginServlet loginControllerServlet, HttpServletRequest request, HttpServletResponse response) {
        log.trace("handleLogout started");
        HttpSession session = request.getSession();
        log.trace("Session state before logout: isLogged = {}, userId = {}", session.getAttribute("isLogged"), session.getAttribute("userId"));
        session.removeAttribute("isLogged");
        log.trace("Session attribute 'isLogged' removed.");
        session.removeAttribute("userId");
        log.trace("Session attribute 'userId' removed.");
        log.trace("Session state after logout: isLogged = {}, userId = {}", session.getAttribute("isLogged"), session.getAttribute("userId"));
        loginControllerServlet.update(response, "success", "Выход выполнен успешно");
    }
//----------------------------------------------------------------------------------------------------------------------
    public synchronized void handleRegister(ControllerRegisterServlet registerControllerServlet, HttpServletResponse response, HttpServletRequest request) {
        log.trace("handleRegister started");
        HttpSession session = request.getSession();

        // Проверяем, что пользователь уже не авторизован.
        log.trace("Session state isLogged: {}", session.getAttribute("isLogged"));
        if (isLogin(request)) {
            registerControllerServlet.update(response, "fail", "Вы уже авторизованы");
            log.trace("User is already logged in.");
            return;
        }

        String login = request.getParameter("login");
        String password = request.getParameter("password");

        // Проверка на не пустое поле логина
        if (login == null || login.isEmpty()) {
            log.trace("Login is empty");
            registerControllerServlet.update(response, "fail", "Логин не может быть пустым");
            return;
        }

        // Проверка на длину логина (минимум 3 символа, максимум 20 символов)
        if (login.length() < 3 || login.length() > 20) {
            log.trace("Login is empty");
            registerControllerServlet.update(response, "fail", "Логин должен быть не менее 3 символов и не более 20 символов");
            return;
        }

        // Проверка на не пустое поле пароля
        if (password == null || password.isEmpty()) {
            log.trace("Password is empty");
            registerControllerServlet.update(response, "fail", "Пароль не может быть пустым");
            return;
        }

        // Проверка на длину пароля (минимум 8 символов, максимум 50 символов)
        if (password.length() < 8 || password.length() > 50) {
            log.trace("Password length is incorrect: {}", password.length());
            registerControllerServlet.update(response, "fail", "Пароль должен быть не менее 8 символов и не более 50 символов");
            return;
        }

        try (Session hibernateSession = HibernateUtil.getSessionFactory().getCurrentSession()) {
            log.trace("Starting a hibernate session");
            hibernateSession.beginTransaction();

            // Используем TypedQuery для избежания предупреждений о deprecated методах
            TypedQuery<User> query = hibernateSession.createQuery("FROM User WHERE login = :login", User.class);
            query.setParameter("login", login);

            User user = null;
            try {
                user = query.getSingleResult(); // Используйте getSingleResult() для получения одного результата
                log.trace("Query result: user {}", user != null ? user.getId() + ", " + user.getLogin() : "not found");
            } catch (NoResultException e){
                // если нет пользователя с таким логином
                log.trace("Query result: user not found");
            }

            if (user != null) {
                registerControllerServlet.update(response, "fail", "Пользователь с таким логином уже существует");
                log.trace("User with login: {} already exists", login);
            } else {
                User newUser = new User();
                newUser.setLogin(login);
                newUser.setPassword(password);
                hibernateSession.persist(newUser); // Используйте persist для создания новых объектов
                log.trace("User with login {} registered successful", login);
                log.trace("New user saved to database with login: {}", newUser.getLogin());
                registerControllerServlet.update(response, "success", "Регистрация успешна");
            }

            hibernateSession.getTransaction().commit();
            log.trace("Transaction commited");
        } catch (Exception e) {
            log.error("Error processing registration request: " + e.getMessage(), e);
            log.error("Error processing registration request: " + e.toString());
//            System.err.println("Error processing registration request: " + e.getMessage());
            e.printStackTrace();
            registerControllerServlet.update(response, "fail", "Ошибка при работе с базой данных");

        }
        log.trace("handleRegister completed");
    }
//----------------------------------------------------------------------------------------------------------------------
    public void handleDelete(ControllerDeleteAccServlet controllerServlet, HttpServletResponse response, HttpServletRequest request) {
        log.trace("handleDelete started");
        HttpSession session = request.getSession();
        Boolean isLogin = (Boolean) session.getAttribute("isLogged");
        Long userId = (Long) session.getAttribute("userId");
        log.trace("Session state before delete: isLogged = {}, userId = {}", isLogin, userId);


        if (isLogin != null && isLogin) {
            log.trace("User is logged in, attempting to delete");
            try (Session hibernateSession = HibernateUtil.getSessionFactory().openSession()) {
                hibernateSession.beginTransaction();

                User user = hibernateSession.find(User.class, userId);
                log.trace("User found: {}", user != null ? user.getId() + ", " + user.getLogin() : "not found");

                if (user != null) {
                    // Удалить пользователя
                    log.trace("Deleting user: {}", user.getId() + ", " + user.getLogin());
                    hibernateSession.remove(user);

                    // Обновить записи в таблице Url
                    TypedQuery<Url> query = hibernateSession.createQuery("FROM Url WHERE userId = :userId", Url.class);
                    query.setParameter("userId", userId);

                    List<Url> urls = query.getResultList();
                    log.trace("Found {} URLs to update", urls.size());
                    for (Url url : urls) {
                        log.trace("Setting userId to null for Url with id {}", url.getId());
                        url.setUserId(null);
                        hibernateSession.merge(url);
                    }

                    hibernateSession.getTransaction().commit();
                    log.trace("Transaction committed");


                    session.removeAttribute("isLogged");
                    session.removeAttribute("userId");
                    log.trace("Session state after delete: isLogged = {}, userId = {}", session.getAttribute("isLogged"), session.getAttribute("userId"));
                    controllerServlet.update(response, "success", "Пользователь успешно удален");
                } else {
                    controllerServlet.update(response, "fail", "Пользователь не найден");
                    log.trace("User not found, sending fail message to controller");
                }
            }  catch (Exception e) {
//                System.err.println("Error during deletion: " + e.getMessage());
                log.error("Error during deletion: " + e.getMessage(), e);
                log.error("Error during deletion: " + e.toString());
                e.printStackTrace();
                controllerServlet.update(response, "fail", "Database error during deletion");
            }
        } else {
            controllerServlet.update(response, "fail", "Вы не авторизованы");
            log.trace("User is not logged in. Sending 'not authorized' message to controller");
        }
        log.trace("handleDelete completed.");
    }
//----------------------------------------------------------------------------------------------------------------------
    public void handleListLinks(ControllerListServlet listControllerServlet, HttpServletResponse response, HttpServletRequest request, String host) {
        log.trace("handleListLinks started");
        HttpSession session = request.getSession();
        Boolean isLogin = (Boolean) session.getAttribute("isLogged");
        Long userId = (Long) session.getAttribute("userId");
        log.trace("Session state: isLogged = {}, userId = {}", isLogin, userId);

        if (isLogin != null && isLogin) {
            try (Session hibernateSession = HibernateUtil.getSessionFactory().openSession()) {
                log.trace("Starting hibernate session and transaction");
                hibernateSession.beginTransaction();
                log.trace("Attempting to find user with id {}", userId);
                User user = findUserById(hibernateSession, userId);
                log.trace("findUserById result: user {}", user != null ? user.getId() + ", " + user.getLogin() : "not found");

                if (user != null) {
                    log.trace("User with id: {} and login: {} is found", user.getId(), user.getLogin());
                    String login = user.getLogin();

                    List<Url> urls = getPaginatedUrls(hibernateSession, userId, request);
                    log.trace("Retrieved {} URLs for user with id: {}", urls.size(), userId);
                    LinkedHashMap<String, String> json = new LinkedHashMap<>();
                    for (Url url : urls) {
                        String originalUrl = url.getOriginalUrl();
                        log.trace("Decoding URL: {}", originalUrl);
                        try {
                            originalUrl = URLDecoder.decode(originalUrl, "UTF-8");
                            log.trace("Decoded URL: {}", originalUrl);
                        } catch (UnsupportedEncodingException e) {
//                            System.err.println("Error decoding URL: " + e.getMessage());
                            log.error("Error decoding URL: " + e.getMessage(), e);
                            //Consider logging the full stack trace as well e.printStackTrace();
                        }
                        json.put(host + "/s/" + url.getShortUrl(), originalUrl);
                        log.trace("Added to json: shortUrl: {}, originalUrl: {}", host + "/s/" + url.getShortUrl(), originalUrl);
                    }
                    ObjectMapper mapper = new ObjectMapper();
                    String jsonObject = mapper.writeValueAsString(json);
                    long totalRecords = getTotalUrlCount(hibernateSession, userId);
                    log.trace("Total url count: {}", totalRecords);
                    int totalPages = calculateTotalPages(totalRecords, 7);
                    int pageNumber = request.getParameter("pageNumber")!= null? Integer.parseInt(request.getParameter("pageNumber")) : 1;

                    listControllerServlet.update(response, login, jsonObject, pageNumber, totalPages);
                    log.trace("Sending update to controller: login = {}, json = {}, pageNumber = {}, totalPages = {}", login, jsonObject, pageNumber, totalPages );
                } else {
                    log.trace("User is not logged in, sending not authorized error to controller");
                    listControllerServlet.update(response, "fail", "Пользователь не найден", 1, 1);
                }
                hibernateSession.getTransaction().commit();
                log.trace("commited successful");
            } catch (Exception e) {
//                System.err.println("Error processing link list request: " + e.getMessage());
                log.error("Error processing link list request: " + e.getMessage());
                e.printStackTrace();
                listControllerServlet.update(response, "fail", "Database error", 1, 1);
            }
        } else {
            listControllerServlet.update(response, "fail", "Вы не авторизованы", 1, 1);
        }
        log.trace("handleListLinks completed");
    }
    private User findUserById(Session session, Long userId) {
        try {
            TypedQuery<User> query = session.createQuery("FROM User WHERE id = :userId", User.class);
            query.setParameter("userId", userId);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    private long getTotalUrlCount(Session session, Long userId) {
        TypedQuery<Long> countQuery = session.createQuery("SELECT COUNT(*) FROM Url WHERE userId = :userId", Long.class);
        countQuery.setParameter("userId", userId);
        return countQuery.getSingleResult();
    }
    private int calculateTotalPages(long totalRecords, int pageSize) {
        return (int) Math.ceil((double) totalRecords / pageSize);
    }
    private List<Url> getPaginatedUrls(Session session, Long userId, HttpServletRequest request) {
        TypedQuery<Url> urlQuery = session.createQuery("FROM Url WHERE userId = :userId ORDER BY createdAt DESC", Url.class);
        urlQuery.setParameter("userId", userId);
        int pageSize = 7;
        int pageNumber = request.getParameter("pageNumber")!= null? Integer.parseInt(request.getParameter("pageNumber")) : 1;

        int start = (pageNumber - 1) * pageSize;
        urlQuery.setFirstResult(start);
        urlQuery.setMaxResults(pageSize);

        return urlQuery.getResultList();
    }
//----------------------------------------------------------------------------------------------------------------------
    public String getOriginalLink(String shortLink) {
        log.trace("getOriginalLink started, shortLink: {}", shortLink);
        try (Session session = HibernateUtil.getSessionFactory().getCurrentSession()) {
            log.trace("Starting hibernate session and transaction");
            session.beginTransaction();

            Url existingUrl = (Url) session.createQuery("FROM Url WHERE shortUrl = :shortUrl", Url.class)
                    .setParameter("shortUrl", shortLink)
                    .getSingleResult();
            log.trace("Query result: user {}", existingUrl != null ? existingUrl.getId() + ", " + existingUrl.getShortUrl() : "not found");
            session.getTransaction().commit();
            log.trace("Transaction committed");
            try {
                String decodedUrl = URLDecoder.decode(existingUrl.getOriginalUrl(), "UTF-8");
                log.trace("Decoded URL: {}", decodedUrl);
                log.trace("Returning URL: {}", decodedUrl);
                return decodedUrl;
            } catch (UnsupportedEncodingException e) {
//                System.out.println("Ошибка при декодировании ссылки: " + e.getMessage());
                log.error("Error decoding URL: " + e.getMessage(), e);
                return null;
            }

        } catch (NoResultException e) {
            log.trace("Url with shortLink: {} not found", shortLink);
            return null;
        } catch (Exception e) {
            log.error("Error processing request: " + e.getMessage(), e);
//            System.out.println("Ошибка при обработке запроса: " + e.getMessage());
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
//--------------------------------------------------------------------------------------------------------------------------
//    public void testLoggerring(){
//        log.trace("xxxxxxxxxxyyyy");
//        System.out.println("xxxxxxxxxxxyyyyy");
//    }
}