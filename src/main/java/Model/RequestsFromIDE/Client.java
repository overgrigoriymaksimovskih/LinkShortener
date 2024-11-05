package Model.RequestsFromIDE;

import java.net.http.HttpClient;
import java.time.Duration;

public class Client {
    HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(200))
            .build();

    public HttpClient getClient() {
        return client;
    }
}
