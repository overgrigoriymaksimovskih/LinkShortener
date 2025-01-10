package DAOLayer;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class DbInitializer implements ServletContextListener {
    private DbCreator dbCreator;
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();
        if(servletContext!=null){
            dbCreator = new DbCreator(servletContext);
            dbCreator.setUpDB();
            // Сохраняем DbCreator в ServletContext
            servletContext.setAttribute("dbCreator", dbCreator);
        }

    }
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}