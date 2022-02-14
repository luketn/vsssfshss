package com.mycodefu.vsssfshss.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycodefu.vsssfshss.server.MessageSender;

public class ChatMessageHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void handleMessage(String fromName, String message, MessageSender messageSender) {
        try {
            ChatMessage chatMessage = objectMapper.readValue(message, ChatMessage.class);
            ChatMessage broadcastMessage = new ChatMessage(fromName, chatMessage.message());
            messageSender.broadcast(objectMapper.writeValueAsString(broadcastMessage));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
