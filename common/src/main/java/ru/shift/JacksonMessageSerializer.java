package ru.shift;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonMessageSerializer implements MessageSerializer {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String serialize(ChatMessage message) {
        try {
            return mapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize ChatMessage to JSON", e);
        }
    }

    @Override
    public ChatMessage deserialize(String data) {
        try {
            return mapper.readValue(data, ChatMessage.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize ChatMessage from JSON: " + data, e);
        }
    }
}
