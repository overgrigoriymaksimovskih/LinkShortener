package DAOLayer;

import DAOLayer.Entity.Url;
import jakarta.persistence.NoResultException;
import org.hibernate.Session;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


    @WebServlet(name = "ShortLinkServlet", urlPatterns = "/s/*") public class ShortLinkServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String shortLink = req.getRequestURI().substring(req.getContextPath().length() + req.getServletPath().length() + 1);
        String originalLink = getOriginalLink(shortLink);
        System.out.println(shortLink);
        System.out.println(originalLink);

            if (originalLink!= null) {
                resp.sendRedirect(originalLink);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        }

    private String getOriginalLink(String shortLink) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        try {
            session.beginTransaction();

            System.out.println("Выполняем запрос к базе данных...");

            Url existingUrl = (Url) session.createQuery("FROM Url WHERE shortUrl = :shortUrl", Url.class)
                    .setParameter("shortUrl", shortLink)
                    .getSingleResult();

            System.out.println("Запрос к базе данных выполнен. Результат: " + existingUrl);

            session.getTransaction().commit();

            return existingUrl.getOriginalUrl();
        } catch (NoResultException e) {
            System.out.println("Запись не найдена в базе данных.");
            return null;
        } catch (Exception e) {
            System.out.println("Ошибка при оообработке запроса: " + e.getMessage());
            return null;
        } finally {
            if (session!= null) {
                session.close();
            }
        }
    }
}
