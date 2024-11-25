package DAOLayer;

import Controller.LinkController.ControllerServlet;
import DAOLayer.Entity.Url;
import org.hibernate.Session;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;

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
    public void handleLink(ControllerServlet controllerServlet, HttpServletResponse response, String link, String host) throws IOException {

        try (Session session = HibernateUtil.getSessionFactory().getCurrentSession()) {
            session.beginTransaction();

            Long count = (Long) session.createQuery("SELECT COUNT(*) FROM Url WHERE originalUrl = :originalUrl")
                    .setParameter("originalUrl", link)
                    .getSingleResult();

            if (count > 0) {
                // Если короткая ссылка уже существует, использовать ее
                Url existingUrl = (Url) session.createQuery("FROM Url WHERE originalUrl = :originalUrl")
                        .setParameter("originalUrl", link)
                        .getSingleResult();
                String shortUrl = existingUrl.getShortUrl();

                //Делаем человекопонятный вид. Отправляем в вьюху
                controllerServlet.update(response, host + "/s/" + shortUrl);
            } else {
                // Если короткая ссылка не существует, создать новую
                Url url = new Url();
                url.setOriginalUrl(link);
                url.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                url.setUserId(null);
                session.save(url);

                // Получить значение столбца AUTO_INCREMENT
                long id = url.getId();

                // Преобразовать значение столбца AUTO_INCREMENT в сокращенный URL
                String shortUrl = ShortUrlGenerator.generateShortUrl(id);

                // Обновить запись в таблице с сокращенным URL
                url.setShortUrl(shortUrl);
                session.update(url);

                //Делаем человекопонятный вид. Отправляем в вьюху
                System.out.println(host + "/s/" + shortUrl);
                controllerServlet.update(response, host + "/s/" + shortUrl);
            }

            session.getTransaction().commit();

        } catch (Exception e) {
            System.out.println("Ошибко при обработке запроса: " + e.getMessage());
            controllerServlet.update(response, "ошибка при работе с базой данных");
            return;
        }
    }
}