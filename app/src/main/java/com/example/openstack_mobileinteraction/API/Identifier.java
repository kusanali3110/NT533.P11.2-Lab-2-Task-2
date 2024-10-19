package com.example.openstack_mobileinteraction.API;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Identifier {
    public String getToken(String username, String password, String domain) throws MalformedURLException, ExecutionException, InterruptedException {
        String token = null;

        // Define parameters
        URL url = new URL("http://192.168.229.133/identity/v3/auth/tokens");
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        String inputBody = "{\n" +
                "    \"auth\": {\n" +
                "        \"identity\": {\n" +
                "            \"methods\": [\n" +
                "                \"password\"\n" +
                "            ],\n" +
                "            \"password\": {\n" +
                "                \"user\": {\n" +
                "                    \"name\": \"" + username + "\",\n" +
                "                    \"domain\": {\n" +
                "                        \"name\": \"" + domain + "\"\n" +
                "                    },\n" +
                "                    \"password\": \"" + password + "\"\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";

        // Process HttpRequest/Response
        HttpHandler httpHandler = new HttpHandler();
        Map<String, String> responseHeaders = httpHandler.getResponseHeaders(url, "POST", headers, inputBody);

        if (responseHeaders == null) return null;
        else {
            token = responseHeaders.get("X-Subject-Token");
        }

        return token;
    }
}
