package DAOLayer;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.net.URI;
import java.net.URL;

@WebListener
public class DbInitializer implements ServletContextListener {
    private DbCreator dbCreator;
    @Override
    public void contextInitialized(ServletContextEvent sce) {

        ServletContext servletContext = sce.getServletContext();
//        initLog4j2(servletContext);

        if(servletContext!=null){
            dbCreator = new DbCreator(servletContext);
            dbCreator.setUpDB();
            // Сохраняем DbCreator в ServletContext
            servletContext.setAttribute("dbCreator", dbCreator);
        }

    }
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
//        // Очистить контекст log4j
//        LoggerContext context = (LoggerContext) LogManager.getContext(false);
//        context.stop();
    }

    //  Инициализируем Log4j2
//    private void initLog4j2(ServletContext servletContext) {
//        try {
//            String configPath = servletContext.getRealPath("/WEB-INF/classes/log4j2.xml");
//            File configFile = new File(configPath);
//            URI uri = configFile.toURI();
//            LoggerContext context = (LoggerContext) LogManager.getContext(false);
//            context.setConfigLocation(uri);
//            System.out.println("Log4j2 is initialized!");
//        } catch (Exception e) {
//            System.err.println("Error initializing Log4j2: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
}