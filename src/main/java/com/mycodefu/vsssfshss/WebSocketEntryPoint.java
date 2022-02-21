package com.mycodefu.vsssfshss;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.enterprise.context.ApplicationScoped;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycodefu.vsssfshss.chat.ChatMessage;
import com.mycodefu.vsssfshss.names.NameGenerator;

@ServerEndpoint("/ws")
@ApplicationScoped
public class WebSocketEntryPoint {
    private Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Got connection from " + session.getId());
        String name = NameGenerator.generateName();
        session.getUserProperties().put("name", name);
        sessions.put(name, session);
        try {
            sendMessage(new ChatMessage("server", "Welcome " + name + "!"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session) {
        String name = (String) session.getUserProperties().get("name");
        System.out.println(name + " disconnected");
        sessions.remove(name);
        try {
            sendMessage(new ChatMessage("server", "Goodbye " + name + "!"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("Error!");
        throwable.printStackTrace();
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            String name = (String) session.getUserProperties().get("name");
            ChatMessage incomingMessage = objectMapper.readValue(message, ChatMessage.class);
            sendMessage(new ChatMessage(name, incomingMessage.message()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(ChatMessage msg) throws Exception {
        String stringMessage = objectMapper.writeValueAsString(msg);
        sessions.values().forEach(session -> session.getAsyncRemote().sendText(stringMessage));
    }
}
