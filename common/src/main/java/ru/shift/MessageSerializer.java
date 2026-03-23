package ru.shift;

public interface MessageSerializer {
    String serialize(ChatMessage message);
    ChatMessage deserialize(String data);
}
