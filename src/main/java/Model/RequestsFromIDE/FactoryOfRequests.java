package Model.RequestsFromIDE;

import java.net.http.HttpRequest;

/**
 * Hello world!
 *
 */
public class FactoryOfRequests {

    public static HttpRequest createRequest (RequestTypes typeOfRequest){
        if (typeOfRequest == RequestTypes.GETOBJECTID){
            return new RequestGetObject().getRequest();
        }else{
            return null;
        }
    }
}