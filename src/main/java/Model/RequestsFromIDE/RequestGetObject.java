package Model.RequestsFromIDE;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;

import static java.time.temporal.ChronoUnit.MILLIS;

public class RequestGetObject implements Requestable {
    @Override
    public HttpRequest getRequest() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/LinkShortener"))
                .version(HttpClient.Version.HTTP_2)
                .timeout( Duration.of(100000, MILLIS) )
                .GET()
                .build();
        return request;
    }
}
