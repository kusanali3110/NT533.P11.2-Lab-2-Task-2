package com.example.openstack_mobileinteraction.API;

import android.icu.util.Output;
import android.util.Pair;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.URL;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;

public class HttpHandler {
    private Map<String, String> responseHeaders = new HashMap<String, String>();
    private Pair<Integer, String> responseBody;

    public Map<String, String> getResponseHeaders(URL url, String method, Map<String, String> headers, String body) {
        try {
            responseHeaders.clear();

            // Send request
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod(method);
            for (Map.Entry<String, String> header : headers.entrySet()) {
                httpConn.setRequestProperty(header.getKey(), header.getValue());
            }
            httpConn.setDoOutput(true);
            OutputStream os = httpConn.getOutputStream();
            byte[] input = body.getBytes("utf8");
            os.write(input, 0, input.length);
            os.flush();

            // Get headers
            httpConn.getResponseCode();
            for (int i = 0;; i++) {
                String headerKey = httpConn.getHeaderFieldKey(i);
                String headerValue = httpConn.getHeaderField(i);
                if (headerKey == null && headerValue == null) {
                    break;
                }
                responseHeaders.put(headerKey, headerValue);
            }
            return responseHeaders;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Pair<Integer, String> getResponseBody(URL url, String method, Map<String, String> headers, String body) {
        HttpURLConnection httpConn = null;
        try {
            // Send request
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod(method);

            // Add headers
            for (Map.Entry<String, String> header : headers.entrySet()) {
                httpConn.setRequestProperty(header.getKey(), header.getValue());
            }

            // Send request body only for POST or PUT methods
            if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
                httpConn.setDoOutput(true);
                if (body != null && !body.isEmpty()) {
                    try (OutputStream os = httpConn.getOutputStream()) {
                        byte[] input = body.getBytes("utf8");
                        os.write(input, 0, input.length);
                        os.flush();
                    }
                }
            }

            // Get response code
            int statusCode = httpConn.getResponseCode();

            // Get response body (normal stream for 2xx, error stream otherwise)
            InputStream stream;
            if (statusCode >= 200 && statusCode < 300) {
                stream = httpConn.getInputStream();  // success stream
            } else {
                stream = httpConn.getErrorStream();  // error stream
            }

            // Read the response
            BufferedReader buffReader = new BufferedReader(new InputStreamReader(stream));
            String inputLine;
            StringBuilder responseBuilder = new StringBuilder();
            while ((inputLine = buffReader.readLine()) != null) {
                responseBuilder.append(inputLine);
            }
            buffReader.close();

            // Return the status code and response body
            return new Pair<>(statusCode, responseBuilder.toString());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (httpConn != null) {
                httpConn.disconnect();
            }
        }
    }
}
