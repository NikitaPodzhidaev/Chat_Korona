package ru.shift;


public interface MessageListener {
    void onMessage(ChatMessage message);
    void onError(String errorText);
}
