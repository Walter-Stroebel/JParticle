/*
 * Copyright (c) 2017 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.jparticle;

import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Represents a Device.
 * @author walter
 */
public class Device {

    public final boolean cellular;
    public final String id;
    public final JSONArray functions;
    public final int platformId;
    public String lastApp;
    public final int productId;
    public final String status;
    public boolean connected;
    public Date lastHeard;
    public String name;
    public final InetAddress lastIPAddress;
    public final JSONObject variables;

    /**
     * Called by the Cloud object to register a device.
     * @param o JSON as received from the cloud.
     * @throws Exception Obviously.
     */
    public Device(JSONObject o) throws Exception {
        cellular = o.getBoolean("cellular");
        id = o.getString("id");
        functions = o.optJSONArray("functions");
        platformId = o.getInt("platform_id");
        lastApp = o.optString("last_app");
        productId = o.getInt("product_id");
        status = o.getString("status");
        connected = o.getBoolean("connected");
        lastHeard = Cloud.parseDateTime(o.optString("last_heard"));
        name = o.getString("name");
        lastIPAddress = InetAddress.getByName(o.getString("last_ip_address"));
        variables = o.optJSONObject("variables");
    }

    /**
     * Obtain a device by directly calling the cloud.
     * @param deviceId Device ID
     * @param accessToken Your access token. Should start with Bearer.
     * @return A Device.
     * @throws Exception Probably if something did not work. 
     */
    public static Device getDevice(String deviceId, String accessToken) throws Exception {
        URL url = new URL("https://api.particle.io/v1/devices/" + deviceId);
        URLConnection conn = url.openConnection();
        conn.setRequestProperty("Authorization", accessToken);
        conn.connect();
        return new Device(new JSONObject(new JSONTokener(conn.getInputStream())));
    }

    /** Get all your devices.
     * 
     * @param accessToken
     * @param accessToken Your access token. Should start with Bearer.
     * @return A list of Devices.
     * @throws Exception Probably if something did not work. 
     */
    public static ArrayList<Device> getDevices(String accessToken) throws Exception {
        URL url = new URL("https://api.particle.io/v1/devices");
        URLConnection conn = url.openConnection();
        conn.setRequestProperty("Authorization", accessToken);
        conn.connect();
        JSONArray ja = new JSONArray(new JSONTokener(conn.getInputStream()));
        ArrayList<Device> ret = new ArrayList<>();
        for (int i = 0; i < ja.length(); i++) {
            ret.add(new Device(ja.getJSONObject(i)));
        }
        return ret;
    }

    /**
     * Request a boolean variable.
     * @param name Name of the variable.
     * @param accessToken Your access token. Should start with Bearer.
     * @return The boolean value.
     * @throws Exception On errors.
     */
    public Boolean readBoolean(String name, String accessToken) throws Exception {
        URL url = new URL("https://api.particle.io/v1/devices/" + id + "/" + name);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", accessToken);
        conn.setDoOutput(false);
        int resp = conn.getResponseCode();
        if (resp == 200) {
            JSONObject jo = new JSONObject(new JSONTokener(conn.getInputStream()));
            updateFields(jo);
            return jo.getBoolean("result");
        }
        return null;
    }

    private void updateFields(JSONObject jo) {
        try {
            JSONObject core = jo.getJSONObject("coreInfo");
            if (!core.getString("deviceID").equals(id)) {
                throw new Exception("Got a response for another device?");
            }
            this.lastApp = core.getString("last_app");
            this.connected = core.getBoolean("connected");
            this.lastHeard = Cloud.parseDateTime(core.optString("last_heard"));
        } catch (Exception ex) {
            System.err.println(jo.toString(4));
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String toString() {
        return "Device{"
                + "\n\tcellular=" + cellular
                + "\n\tid=" + id
                + "\n\tfunctions=" + functions
                + "\n\tplatformId=" + platformId
                + "\n\tlastApp=" + lastApp
                + "\n\tproductId=" + productId
                + "\n\tstatus=" + status
                + "\n\tconnected=" + connected
                + "\n\tlastHeard=" + lastHeard
                + "\n\tname=" + name
                + "\n\tlastIPAddress=" + lastIPAddress
                + "\n\tvariables=" + variables
                + "\n}";
    }

    /** Get a fresh copy of this device.
     * 
     * @param accessToken Your access token.
     * @return A new Device with the most recent values for the fields.
     * @throws Exception 
     */
    public Device refresh(String accessToken) throws Exception {
        return getDevice(id, accessToken);
    }

    /**
     * Call a function on this device.
     *
     * @param functionName The name of the function to call.
     * @param arg The argument(s) for the function.
     * @param accessToken Your access token.
     * @return The value the function returned or null on some failures.
     * @throws Exception On more serious failures.
     */
    public Integer callFunction(String functionName, String arg, String accessToken) throws Exception {
        URL url = new URL("https://api.particle.io/v1/devices/" + id + "/" + functionName);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", accessToken);
        conn.setDoOutput(true);
        try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
            wr.writeBytes("args=");
            wr.writeBytes(URLEncoder.encode(arg, "UTF-8"));
            wr.flush();
        }
        int resp = conn.getResponseCode();
        if (resp == 200) {
            JSONObject jo = new JSONObject(new JSONTokener(conn.getInputStream()));
            if (!jo.getString("id").equals(id)) {
                throw new Exception("Got a response for another device?");
            }
            this.lastApp = jo.getString("last_app");
            this.connected = jo.getBoolean("connected");
            return jo.getInt("return_value");
        }
        return null;
    }

    /**
     * Request an integer variable.
     * @param name Name of the variable.
     * @param accessToken Your access token. Should start with Bearer.
     * @return The integer value.
     * @throws Exception On errors.
     */
    public Integer readInt(String name, String accessToken) throws Exception {
        URL url = new URL("https://api.particle.io/v1/devices/" + id + "/" + name);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", accessToken);
        conn.setDoOutput(false);
        int resp = conn.getResponseCode();
        if (resp == 200) {
            JSONObject jo = new JSONObject(new JSONTokener(conn.getInputStream()));
            updateFields(jo);
            return jo.getInt("result");
        }
        return null;
    }

    /**
     * Request a String variable.
     * @param name Name of the variable.
     * @param accessToken Your access token. Should start with Bearer.
     * @return The String value.
     * @throws Exception On errors.
     */
    public String readString(String name, String accessToken) throws Exception {
        URL url = new URL("https://api.particle.io/v1/devices/" + id + "/" + name);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", accessToken);
        conn.setDoOutput(false);
        int resp = conn.getResponseCode();
        if (resp == 200) {
            JSONObject jo = new JSONObject(new JSONTokener(conn.getInputStream()));
            updateFields(jo);
            return jo.getString("result");
        }
        return null;
    }

    /**
     * Request a double variable.
     * @param name Name of the variable.
     * @param accessToken Your access token. Should start with Bearer.
     * @return The double value.
     * @throws Exception On errors.
     */
    public double readDouble(String name, String accessToken) throws Exception {
        URL url = new URL("https://api.particle.io/v1/devices/" + id + "/" + name);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", accessToken);
        conn.setDoOutput(false);
        int resp = conn.getResponseCode();
        if (resp == 200) {
            JSONObject jo = new JSONObject(new JSONTokener(conn.getInputStream()));
            updateFields(jo);
            return jo.getDouble("result");
        }
        return Double.NaN;
    }

}
