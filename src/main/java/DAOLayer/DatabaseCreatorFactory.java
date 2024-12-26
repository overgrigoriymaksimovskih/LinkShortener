package DAOLayer;

import View.ViewServlet;

public interface DatabaseCreatorFactory {
    DatabaseCreator create(ViewServlet viewServlet);
}