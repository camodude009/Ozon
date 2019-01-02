package io.restlets;

import com.google.gson.Gson;
import org.restlet.*;
import org.restlet.data.*;
import org.restlet.routing.Router;

public class RestletServer extends Application {

    public Restlet createInboundRoot() {
        Gson gson = new Gson();
        Router router = new Router(getContext());
        // creating a quotation resource
        router.attach("/summary", new Restlet() {
            public void handle(Request request, Response response) {
                // only allow POST
                if (request.getMethod().equals(Method.POST)) {
                    // parsing info
                    String json = request.getEntityAsText();
                    ClientInfo info = gson.fromJson(json, ClientInfo.class);
                    // creating summary
                    // TODO
                    // setting response
                    response.setEntity(gson.toJson(""), MediaType.APPLICATION_JSON);
                    response.setStatus(Status.SUCCESS_OK);
                } else response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
            }
        });

        return router;
    }

    public static void main(String[] args) {
        // list of services to start
        RestletServer quotationServices = new RestletServer();

        // port to be incremented for each quotationService
        int port = 9000;
        // starting quotationService
        Component component = new Component();
        component.getServers().add(Protocol.HTTP, port++);
        component.getDefaultHost().
                attach("", new RestletServer());
        try {
            component.start();
        } catch (Exception e) {
        }
    }

}