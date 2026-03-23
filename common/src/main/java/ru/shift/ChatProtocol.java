package ru.shift;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class ChatProtocol {

    private static final DateTimeFormatter TS_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ChatProtocol() {
    }


    public static String now() {
        return LocalDateTime.now().format(TS_FORMATTER);
    }

    public static ChatMessage login(String nickname) {
        ChatMessage msg = new ChatMessage();
        msg.setType(MessageType.LOGIN);
        msg.setFrom(nickname);
        msg.setTimestamp(now());
        return msg;
    }

    public static ChatMessage chat(String from, String text) {
        ChatMessage msg = new ChatMessage();
        msg.setType(MessageType.CHAT);
        msg.setFrom(from);
        msg.setText(text);
        msg.setTimestamp(now());
        return msg;
    }

    public static ChatMessage system(SystemCode code, String text) {
        ChatMessage msg = new ChatMessage();
        msg.setType(MessageType.SYSTEM);
        msg.setSystemCode(code);
        msg.setText(text);
        msg.setTimestamp(now());
        return msg;
    }

    public static ChatMessage system(String text) {
        return system(SystemCode.GENERIC, text);
    }


    public static ChatMessage clientLeft(String nickname) {
        String text = "Клиент " + nickname + " вышел из чата";
        return system(SystemCode.CLIENT_LEFT, text);
    }

    public static ChatMessage users(List<String> users) {
        ChatMessage msg = new ChatMessage();
        msg.setType(MessageType.USERS);
        msg.setUsers(users);
        return msg;
    }
}
