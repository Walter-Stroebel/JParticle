/*
 * Copyright (c) 2017 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.jparticle;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HttpsURLConnection;

/**
 *
 * @author walter
 */
public class Cloud {

    /**
     * Date and Time parser
     */
    private static final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
    /**
     * Handles asynchronous calls to the cloud.
     */
    private static final ExecutorService pool = Executors.newWorkStealingPool();
    /**
     * Handles polling variables or functions
     */
    private static final ScheduledExecutorService poll = Executors.newScheduledThreadPool(0);

    /**
     * Convert a Particle date-time to a Java Date.
     *
     * @param dateString As found in the JSON data.
     * @return A Java Date object.
     */
    public static Date parseDateTime(String dateString) {
        synchronized (fmt) {
            if (dateString == null) {
                return null;
            }
            if (dateString.contains("T")) {
                dateString = dateString.replace('T', ' ');
            }
            if (dateString.contains("Z")) {
                dateString = dateString.replace("Z", "+0000");
            } else {
                dateString = dateString.substring(0, dateString.lastIndexOf(':')) + dateString.substring(dateString.lastIndexOf(':') + 1);
            }
            try {
                return fmt.parse(dateString);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    /**
     * Your accessToken
     */
    public final String accessToken;
    /**
     * Currently registered subscribers
     */
    private final TreeMap<UUID, DeviceEvent> callBacks = new TreeMap<>();
    /**
     * Currently registered poll call-backs
     */
    private final TreeMap<UUID, ScheduledFuture<?>> polls = new TreeMap<>();
    /**
     * Your devices
     */
    public final TreeMap<String, Device> devices = new TreeMap<>();

    /**
     * Constructor.
     *
     * @param accessToken Your access token.
     * @param readMine If true will start a thread to collect publications from
     * your devices.
     * @param readAll If true will start a thread to collect publications of all
     * devices -- not recommended.
     */
    public Cloud(String accessToken, boolean readMine, boolean readAll) {
        this.accessToken = accessToken;
        try {
            for (Device d : Device.getDevices(accessToken)) {
                devices.put(d.name, d);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        if (readMine) {
            new Thread(new PublishedReader(true)).start();
        }
        if (readAll) {
            new Thread(new PublishedReader(false)).start();
        }
    }

    /**
     * Call a function on a device.
     *
     * @param device Device name.
     * @param funcName Function name.
     * @param funcArgs Argument(s) for the function call.
     * @return
     */
    public int call(String device, String funcName, String funcArgs) {
        try {
            return devices.get(device).callFunction(funcName, funcArgs, accessToken);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    /**
     * Async call to a function on a device.
     *
     * @param device Device name.
     * @param funcName Function name.
     * @param funcArgs Argument(s) for the function call.
     * @return A Future to obtain the value from.
     */
    public Future<Integer> callF(final String device, final String funcName, final String funcArgs) {
        return pool.submit(new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                return Cloud.this.call(device, funcName, funcArgs);
            }
        });
    }

    /**
     * Async call to a function on a device. This version ignores the function
     * result.
     *
     * @param device Device name.
     * @param funcName Function name.
     * @param funcArgs Argument(s) for the function call.
     */
    public void callTask(final String device, final String funcName, final String funcArgs) {
        pool.submit(new Runnable() {

            @Override
            public void run() {
                Cloud.this.call(device, funcName, funcArgs);
            }
        });
    }

    /**
     * Get a value for a variable from a device.
     *
     * @param device Device name.
     * @param varName Name of the variable.
     * @return Value of the variable or null on errors.
     */
    public Double getDouble(String device, String varName) {
        try {
            return devices.get(device).readDouble(varName, accessToken);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    /**
     * Async version to get a value for a variable from a device.
     *
     * @param device Device name.
     * @param varName Name of the variable.
     * @return Value of the variable or null on errors.
     */
    public Future<Double> getDoubleF(final String device, final String varName) {
        return pool.submit(new Callable<Double>() {

            @Override
            public Double call() throws Exception {
                return getDouble(device, varName);
            }
        });
    }

    /**
     * Get a value for a variable from a device.
     *
     * @param device Device name.
     * @param varName Name of the variable.
     * @return Value of the variable or null on errors.
     */
    public Boolean getBoolean(String device, String varName) {
        try {
            return devices.get(device).readBoolean(varName, accessToken);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    /**
     * Async version to get a value for a variable from a device.
     *
     * @param device Device name.
     * @param varName Name of the variable.
     * @return Value of the variable or null on errors.
     */
    public Future<Boolean> getBooleanF(final String device, final String varName) {
        return pool.submit(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                return getBoolean(device, varName);
            }
        });
    }

    /**
     * Get a value for a variable from a device.
     *
     * @param device Device name.
     * @param varName Name of the variable.
     * @return Value of the variable or null on errors.
     */
    public Integer getInt(String device, String varName) {
        try {
            return devices.get(device).readInt(varName, accessToken);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    /**
     * Async version to get a value for a variable from a device.
     *
     * @param device Device name.
     * @param varName Name of the variable.
     * @return Value of the variable or null on errors.
     */
    public Future<Integer> getIntF(final String device, final String varName) {
        return pool.submit(new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                return getInt(device, varName);
            }
        });
    }

    /**
     * Get a value for a variable from a device.
     *
     * @param device Device name.
     * @param varName Name of the variable.
     * @return Value of the variable or null on errors.
     */
    public String getString(String device, String varName) {
        try {
            return devices.get(device).readString(varName, accessToken);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    /**
     * Async version to get a value for a variable from a device.
     *
     * @param device Device name.
     * @param varName Name of the variable.
     * @return Value of the variable or null on errors.
     */
    public Future<String> getStringF(final String device, final String varName) {
        return pool.submit(new Callable<String>() {

            @Override
            public String call() throws Exception {
                return getString(device, varName);
            }
        });
    }

    /**
     * Publish an event.
     *
     * @param name Name for the event.
     * @param data Content for the event.
     */
    public void publish(String name, String data) {
        try {
            publishTask(name, data, false, 60);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    /**
     * Publish a private event.
     *
     * @param name Name for the event.
     * @param data Content for the event.
     */
    public void publishPrivate(String name, String data) {
        try {
            publishTask(name, data, true, 60);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    /**
     * Publish an event.
     *
     * @param name Name for the event.
     * @param data Content for the event.
     * @param priv Private event.
     * @param ttl Time to live.
     *
     */
    public void publishTask(final String name, final String data, final boolean priv, final int ttl) {
        pool.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://api.particle.io/v1/devices/events");
                    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Authorization", accessToken);
                    conn.setDoOutput(true);
                    try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                        wr.writeBytes("name=");
                        wr.writeBytes(URLEncoder.encode(name, "UTF-8"));
                        wr.writeBytes("&data=");
                        wr.writeBytes(URLEncoder.encode(data, "UTF-8"));
                        wr.writeBytes("&private=");
                        wr.writeBytes(URLEncoder.encode(Boolean.toString(priv), "UTF-8"));
                        wr.writeBytes("&ttl=");
                        wr.writeBytes(URLEncoder.encode(Integer.toString(ttl), "UTF-8"));
                        wr.flush();
                    }
                    conn.getResponseCode();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    /**
     * Refresh the status of your devices.
     */
    public void refresh() {
        pool.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    for (Device d : devices.values()) {
                        devices.put(d.name, d.refresh(accessToken));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    /**
     * Subscribe to an event.
     *
     * @param cb The call-back object holds all needed parameters.
     */
    public void subscribe(DeviceEvent cb) {
        synchronized (callBacks) {
            callBacks.put(cb.uuid(), cb);
        }
    }

    /**
     * Un-subscribe from an event.
     *
     * @param cb The call-back object holds all needed parameters.
     */
    public void unSubscribe(DeviceEvent cb) {
        synchronized (callBacks) {
            callBacks.remove(cb.uuid());
        }
    }

    /**
     * Poll something,
     *
     * @param poller This is called at the appropriate intervals, as defined by
     * the callback object. Probably you would use this to read a variable or
     * call a function.
     */
    public void poll(DevicePoll poller) {
        synchronized (polls) {
            ScheduledFuture<?> handle = poll.scheduleWithFixedDelay(poller, 0, poller.interval(), TimeUnit.MILLISECONDS);
            polls.put(poller.uuid(), handle);
        }
    }

    /**
     * Cancel calling the poller.
     *
     * @param poller The poller to cancel.
     */
    public void cancel(DevicePoll poller) {
        synchronized (polls) {
            ScheduledFuture<?> handle = polls.remove(poller.uuid());
            if (null != handle) {
                handle.cancel(true);
            }
        }
    }

    /**
     * Background task to process published events.
     */
    private class PublishedReader implements Runnable {

        private final boolean mine;

        public PublishedReader(boolean mine) {
            this.mine = mine;
        }

        @Override
        public void run() {
            try {
                URL url = new URL(mine ? "https://api.particle.io/v1/devices/events" : "https://api.particle.io/v1/events");
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", Cloud.this.accessToken);
                conn.setDoOutput(false);
                try (BufferedReader bfr = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String s;
                    while (null != (s = bfr.readLine())) {
                        if (s.startsWith("event: ")) {
                            String event = s.substring(7);
                            String data = bfr.readLine();
                            if (data.startsWith("data: ")) {
                                synchronized (callBacks) {
                                    AnyJSON aj = new AnyJSON(data.substring(6));
                                    final Event e = new Event(event, aj.getObject());
                                    for (final DeviceEvent cb : callBacks.values()) {
                                        if (null != cb.forDeviceName() && cb.forDeviceName().equals(e.name)) {
                                            pool.submit(new Runnable() {
                                                @Override
                                                public void run() {
                                                    cb.event(e);
                                                }
                                            });
                                        } else if (null != cb.forDeviceId() && cb.forDeviceId().equals(e.coreId)) {
                                            pool.submit(new Runnable() {
                                                @Override
                                                public void run() {
                                                    cb.event(e);
                                                }
                                            });
                                        } else if (null == cb.forDeviceName() && null == cb.forDeviceId()) {
                                            pool.submit(new Runnable() {
                                                @Override
                                                public void run() {
                                                    cb.event(e);
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
