package com.example.openstack_mobileinteraction.API;

import android.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.Key;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkManager {
    public String createNetwork(String token, String networkName) throws MalformedURLException, JSONException {
        // Define parameters
        URL url = new URL("http://192.168.229.133:9696/networking/v2.0/networks");
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Auth-Token", token);
        String inputBody = "{\n" +
                "    \"network\": {\n" +
                "        \"name\": \"" + networkName + "\",\n" +
                "        \"admin_state_up\": true\n" +
                "    }\n" +
                "}";

        // Process request/response
        HttpHandler httpHandler = new HttpHandler();
        Pair<Integer, String> body = httpHandler.getResponseBody(url, "POST", headers, inputBody);

        // Retrieve networkID
        String networkID;
        if (body.first != 201) {
            return null;
        } else {
            JSONObject jsonObject = new JSONObject(body.second);
            String networkDetail = jsonObject.getString("network");
            jsonObject = new JSONObject(networkDetail);
            networkID = jsonObject.getString("id");
        }
        return networkID;
    }

    public String createSubnet(String token, String networkID, String subnetName, String networkAddress) throws MalformedURLException, JSONException {
        // Verify network ID
        if (networkID == null) {
            return null;
        }

        // Define parameters
        URL url = new URL("http://192.168.229.133:9696/networking/v2.0/subnets");
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Auth-Token", token);
        String inputBody = "{\n" +
                "    \"subnet\": {\n" +
                "        \"name\": \"" + subnetName + "\",\n" +
                "        \"network_id\": \"" + networkID + "\",\n" +
                "        \"ip_version\": 4,\n" +
                "        \"cidr\": \"" + networkAddress + "\"\n" +
                "    }\n" +
                "}";

        // Process request/response
        HttpHandler httpHandler = new HttpHandler();
        Pair<Integer, String> body = httpHandler.getResponseBody(url, "POST", headers, inputBody);

        // Retrive subnetID
        String subnetID;
        if (body.first != 201) {
            return null;
        } else {
            JSONObject jsonObject = new JSONObject(body.second);
            String subnetDetail = jsonObject.getString("subnet");
            jsonObject = new JSONObject(subnetDetail);
            subnetID = jsonObject.getString("id");
        }

        return subnetID;
    }

    public String createPort(String token, String networkID, String subnetID, String staticIP) throws MalformedURLException, JSONException {
        // Verify network ID and subnet ID
        if (networkID == null || subnetID == null) {
            return null;
        }

        // Define parameters
        URL url = new URL("http://192.168.229.133:9696/networking/v2.0/ports");
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Auth-Token", token);
        String inputBody = "{\n" +
                "    \"port\": {\n" +
                "        \"admin_state_up\": true,\n" +
                "        \"network_id\": \"" + networkID + "\",\n" +
                "        \"port_security_enabled\": false,\n" +
                "        \"security_groups\": null,\n" +
                "        \"fixed_ips\": [\n" +
                "            {\n" +
                "                \"ip_address\": \"" + staticIP + "\",\n" +
                "                \"subnet_id\": \"" + subnetID + "\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}";

        // Process request/response
        HttpHandler httpHandler = new HttpHandler();
        Pair<Integer, String> body = httpHandler.getResponseBody(url, "POST", headers, inputBody);

        //Retrieve port ID
        String portID;
        if (body.first != 201) {
            return null;
        } else {
            JSONObject jsonObject = new JSONObject(body.second);
            String subnetDetail = jsonObject.getString("port");
            jsonObject = new JSONObject(subnetDetail);
            portID = jsonObject.getString("id");
        }

        return portID;
    }
}
