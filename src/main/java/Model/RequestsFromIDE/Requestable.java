package Model.RequestsFromIDE;

import java.net.http.HttpRequest;

public interface Requestable {
    HttpRequest getRequest();
}
