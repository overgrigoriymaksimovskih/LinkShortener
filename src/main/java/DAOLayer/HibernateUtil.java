package DAOLayer;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateUtil {
    private static final Logger log = LoggerFactory.getLogger(HibernateUtil.class);
    private static SessionFactory sessionFactory;

    static {
        try {
            Configuration configuration = new Configuration();
            configuration.configure("hibernate.cfg.xml");
            sessionFactory = configuration.buildSessionFactory();
            log.info("SessionFactory created successfully");
//            System.out.println("SessionFactory created successfully");
        } catch (HibernateException ex) {
            System.err.println("Initial SessionFactory creation failed." + ex.getMessage());
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex);  // Re-throw для того, чтобы приложение не запустилось при проблемах
        } catch (Throwable ex){
            System.err.println("An unexpected error during SessionFactory initialization: " + ex.getMessage());
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex);
        }

    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            System.err.println("Session Factory is not initialized!");  // Log if sessionFactory is null
            throw new IllegalStateException("Session Factory is not initialized!");
        }
        return sessionFactory;
    }


    public static void shutdown() {
        if(sessionFactory != null && !sessionFactory.isClosed()){
            sessionFactory.close();
        }
    }
}
