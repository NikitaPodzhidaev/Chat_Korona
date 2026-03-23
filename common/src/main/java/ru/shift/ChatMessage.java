package ru.shift;

import java.util.List;

public class ChatMessage {

    private MessageType type;
    private String from;
    private String text;
    private String timestamp;
    private SystemCode systemCode;
    private List<String> users;

    public ChatMessage() {
    }

    public ChatMessage(MessageType type,
                       String from,
                       String text,
                       String timestamp,
                       List<String> users) {
        this.type = type;
        this.from = from;
        this.text = text;
        this.timestamp = timestamp;
        this.users = users;
    }

    public void setSystemCode(SystemCode systemCode) {
        this.systemCode = systemCode;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public void setText(String text) {
        this.text = text;
    }

    public SystemCode getSystemCode() {
        return systemCode;
    }

    public MessageType getType() {
        return type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getText() {
        return text;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }
}
