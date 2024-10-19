package com.example.openstack_mobileinteraction.API;

import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstanceManager {
    public Pair<List<String>, List<String>> getImagesList(String token) throws MalformedURLException, JSONException {
        Pair<List<String>, List<String>> imagesListPair = new Pair(new ArrayList<String>(), new ArrayList<String>());

        // Define parameters
        URL url = new URL("http://192.168.229.133/image/v2/images");
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Auth-Token", token);
        String inputBody = "";

        // Process request/response
        HttpHandler httpHandler = new HttpHandler();
        Pair<Integer, String> body = httpHandler.getResponseBody(url, "GET", headers, inputBody);

        String jsonResponse = body.second;

        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray imagesArray = jsonObject.getJSONArray("images");

        for (int i = 0; i < imagesArray.length(); i++) {
            JSONObject imageObject = imagesArray.getJSONObject(i);
            if (imageObject.has("id") && imageObject.has("name")) {
                String id = imageObject.getString("id");
                String name = imageObject.getString("name");
                imagesListPair.first.add(id);
                imagesListPair.second.add(name);
            }
        }

        return imagesListPair;
    }

    public Pair<List<String>, List<String>> getFlavorsList(String token) throws MalformedURLException, JSONException {
        Pair<List<String>, List<String>> flavorsListPair = new Pair(new ArrayList<String>(), new ArrayList<String>());

        // Define parameters
        URL url = new URL("http://192.168.229.133/compute/v2.1/flavors");
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Auth-Token", token);
        String inputBody = "";

        // Process request/response
        HttpHandler httpHandler = new HttpHandler();
        Pair<Integer, String> body = httpHandler.getResponseBody(url, "GET", headers, inputBody);

        String jsonResponse = body.second;

        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray flavors = jsonObject.getJSONArray("flavors");

        for (int i = 0; i < flavors.length(); i++) {
            JSONObject flavorObject = flavors.getJSONObject(i);
            if (flavorObject.has("id") && flavorObject.has("name")) {
                String id = flavorObject.getString("id");
                String name = flavorObject.getString("name");
                flavorsListPair.first.add(id);
                flavorsListPair.second.add(name);
            }
        }

        return flavorsListPair;
    }

    public List<String> getSecurityGroupsList(String token) throws MalformedURLException, JSONException {
        List<String> securityGroupsList = new ArrayList<>();

        // Define parameters
        URL url = new URL("http://192.168.229.133:9696/networking/v2.0/security-groups");
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Auth-Token", token);
        String inputBody = "";

        // Process request/response
        HttpHandler httpHandler = new HttpHandler();
        Pair<Integer, String> body = httpHandler.getResponseBody(url, "GET", headers, inputBody);

        String jsonResponse = body.second;

        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray securityGroups = jsonObject.getJSONArray("security_groups");

        for (int i = 0; i < securityGroups.length(); i++) {
            JSONObject securityGroupsObject = securityGroups.getJSONObject(i);
            if (securityGroupsObject.has("name")) {
                String description = securityGroupsObject.getString("name");
                securityGroupsList.add(description);
            }
        }

        return securityGroupsList;
    }

    public List<String> getPortsList(String token) throws MalformedURLException, JSONException {
        List<String> portsList = new ArrayList<>();

        // Define parameters
        URL url = new URL("http://192.168.229.133:9696/networking/v2.0/ports");
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Auth-Token", token);
        String inputBody = "";

        // Process request/response
        HttpHandler httpHandler = new HttpHandler();
        Pair<Integer, String> body = httpHandler.getResponseBody(url, "GET", headers, inputBody);

        String jsonResponse = body.second;

        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray ports = jsonObject.getJSONArray("ports");

        for (int i = 0; i < ports.length(); i++) {
            JSONObject portsObject = ports.getJSONObject(i);
            if (portsObject.has("id")) {
                String description = portsObject.getString("id");
                portsList.add(description);
            }
        }

        return portsList;
    }

    public String createInstance(String token, String instanceName, String imageID, String flavorID, String securityName, String portID, String customScript) throws MalformedURLException, JSONException {
        // Get networkID from portID

        URL url = new URL("http://192.168.229.133:9696/networking/v2.0/ports?id=" + portID);
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Auth-Token", token);
        String inputBody = "";

        HttpHandler httpHandler = new HttpHandler();
        String networkID = null;
        Pair<Integer, String> body =  httpHandler.getResponseBody(url, "GET", headers, inputBody);
        if (body.first != 200) {
            return null;
        } else {
            JSONObject jsonObject = new JSONObject(body.second);
            JSONArray portDetails = jsonObject.getJSONArray("ports");
            networkID = portDetails.getJSONObject(0).getString("network_id");
        }

        // Post request to create Instance
        url = new URL("http://192.168.229.133/compute/v2.1/servers");
        inputBody = "{\n" +
                "    \"server\" : {\n" +
                "        \"name\" : \"" + instanceName + "\",\n" +
                "        \"imageRef\" : \"" + imageID + "\",\n" +
                "        \"flavorRef\" : \"" + flavorID + "\",\n" +
                "        \"security_groups\" : [\n" +
                "           {\n" +
                "               \"name\" : \"" + securityName + "\"\n" +
                "           }\n" +
                "        ],\n" +
                "        \"networks\" : [{\n" +
                "            \"uuid\" : \"" + networkID + "\",\n" +
                "            \"port\": \"" + portID + "\"\n" +
                "        }],\n" +
                "        \"user_data\" : \"" + customScript + "\"\n" +
                "   }\n" +
                "}";

        body = httpHandler.getResponseBody(url, "POST", headers, inputBody);
        String instanceID;
        if (body.first != 202) {
            return null;
        } else {
            JSONObject bodyObject = new JSONObject(body.second);
            instanceID = bodyObject.getJSONObject("server").getString("id");
        }
        return instanceID;
    }
}
