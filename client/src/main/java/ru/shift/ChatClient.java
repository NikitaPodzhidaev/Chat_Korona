package ru.shift;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;

public class ChatClient {

    private static final Logger log = LoggerFactory.getLogger(ChatClient.class);

    private final String host;
    private final int port;
    private final String nickname;
    private final MessageListener listener;

    private MessageConnection connection;

    public ChatClient(String host, int port, String nickname, MessageListener listener) {
        this.host = host;
        this.port = port;
        this.nickname = nickname;
        this.listener = listener;
    }

    public void connect() throws IOException {
        Socket socket = new Socket(host, port);
        connection = new SocketMessageConnection(socket, new JacksonMessageSerializer());

        log.info("Клиент '{}' подключился к серверу {}:{}", nickname, host, port);

        ChatMessage loginMsg = ChatProtocol.login(nickname);
        connection.send(loginMsg);
        log.info("Клиент '{}' отправил запрос логина", nickname);

        Thread readerThread = new Thread(this::readLoop, "ChatClient-Reader");
        readerThread.start();
    }

    private void readLoop() {
        try {
            ChatMessage msg;
            while ((msg = connection.receive()) != null) {
                listener.onMessage(msg);
            }
            log.info("Соединение с сервером {}:{} закрыто (EOF)", host, port);
        } catch (IOException e) {
            log.warn("Соединение с сервером {}:{} прервано: {}",
                    host, port, e.getMessage());
            listener.onError("Соединение прервано: " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("Ошибка протокола на клиенте '{}': {}", nickname, e.getMessage(), e);
            listener.onError("Ошибка протокола: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    public void sendText(String text) {
        if (connection == null) {
            log.warn("Попытка отправить сообщение при неустановленном соединении (ник='{}')", nickname);
            listener.onError("Соединение ещё не установлено");
            return;
        }

        ChatMessage msg = ChatProtocol.chat(nickname, text);
        try {
            connection.send(msg);
            log.debug("Клиент '{}' отправил сообщение: {}", nickname, text);
        } catch (Exception e) {
            log.warn("Клиент '{}' не смог отправить сообщение: {}", nickname, e.getMessage());
            listener.onError("Не удалось отправить сообщение: " + e.getMessage());
        }
    }

    public void disconnect() {
        try {
            if (connection != null) {
                connection.close();
                log.info("Клиент '{}' отключился от сервера {}:{}", nickname, host, port);
                connection = null;
            }
        } catch (IOException e) {
            log.debug("Ошибка при закрытии соединения клиента '{}': {}", nickname, e.getMessage());
        }
    }
}
