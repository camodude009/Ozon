package io.restlets;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.restlet.resource.ClientResource;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class RestletClient {
    public static void main(String[] args) throws IOException {
        Gson gson = new Gson();
        // creating RestletClient
        ClientResource client = new ClientResource("localhost");
        // making post request
        String json = client.post(gson.toJson("request goes here")).getText();
        // parsing response
        Type type = new TypeToken<List<String>>() {
        }.getType();
        List<String> quotations = gson.fromJson(json, type);
    }
}
