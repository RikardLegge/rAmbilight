package com.rambilight.plugins.PushBullet;

import org.glassfish.tyrus.client.ClientManager;

import javax.websocket.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

@ClientEndpoint
public class PushBulletEndpoint {

    static         PushBulletEndpointListener eventListener;
    private static String                     apiKey;
    private static Session                    session;

    public static void setListener(PushBulletEndpointListener eventListener) {
        PushBulletEndpoint.eventListener = eventListener;
    }

    public static void open() {
        if (session != null)
            close();
        ClientManager client = ClientManager.createClient();
        try {
            session = client.connectToServer(PushBulletEndpoint.class, new URI("wss://stream.pushbullet.com/websocket/" + apiKey));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void close() {
        try {
            session.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        session = null;
    }

    public static String getHistory() {
        URL url;
        HttpURLConnection conn;
        BufferedReader rd;
        String line;
        String result = "";
        try {
            url = new URL("https://api.pushbullet.com/v2/pushes?modified_after=0");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "Bearer " + PushBulletEndpoint.apiKey);
            conn.setRequestMethod("GET");
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = rd.readLine()) != null) {
                result += line;
            }
            rd.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @OnOpen
    public void onOpen(Session session) {
        eventListener.onConnected();
        System.out.println("Endpoint open");
    }

    @OnMessage
    public String onMessage(String message, Session session) {
        if (message.contains("tickle"))
            ;
        if (message.contains("push")) {
            int start = message.indexOf("\"application_name\"");
            if (start > 0) {
                start = message.indexOf(":", start);
                int end = message.indexOf("\"", start + 2);
                eventListener.onMessage(message.substring(start + 2, end));
            }
        }
        return message;
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("Endpoint closed");
        if (closeReason.getCloseCode() != CloseReason.CloseCodes.NORMAL_CLOSURE) {
            eventListener.onDisconnected("Reopening...");
            open();
        }
        else
            eventListener.onDisconnected("");

    }

    interface PushBulletEndpointListener {

        public void onConnected();

        public void onDisconnected(String s);

        public void onMessage(String s);

        public void onError(String s);
    }

    public static void setAPiKey(String apiKey) {
        PushBulletEndpoint.apiKey = apiKey;
    }
}
