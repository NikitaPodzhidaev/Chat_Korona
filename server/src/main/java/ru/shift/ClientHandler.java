package ru.shift;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ClientHandler.class);

    private final Socket socket;
    private final ChatServer server;

    private MessageConnection connection;
    private String nickname;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            connection = new SocketMessageConnection(socket, new JacksonMessageSerializer());

            if (!handleLogin()) {
                return;
            }

            server.broadcastSystem("Клиент " + nickname + " присоединился к чату");
            server.broadcastUserList();

            listenMessages();

        } catch (IOException e) {
            log.warn("Ошибка соединения с клиентом {} (ник={}): {}",
                    socket.getRemoteSocketAddress(), nickname, e.getMessage());
        } catch (RuntimeException e) {
            log.error("Критическая ошибка при обработке клиента {} (ник={})",
                    socket.getRemoteSocketAddress(), nickname, e);
        } finally {
            cleanup();
        }
    }

    public void send(ChatMessage msg) {
        if (connection == null) {
            log.warn("Попытка отправить сообщение клиенту {}, но соединение уже закрыто", nickname);
            return;
        }
        try {
            connection.send(msg);
        } catch (Exception e) {
            log.warn("Не удалось отправить сообщение клиенту {}: {}", nickname, e.getMessage());
        }
    }

    private boolean handleLogin() throws IOException {
        ChatMessage loginMsg = connection.receive();

        if (loginMsg == null) {
            log.info("Клиент {} отключился до авторизации", socket.getRemoteSocketAddress());
            return false;
        }

        if (loginMsg.getType() != MessageType.LOGIN) {
            log.warn("От клиента {} ожидается LOGIN, но получен тип {}",
                    socket.getRemoteSocketAddress(), loginMsg.getType());
            connection.send(ChatProtocol.system("Ожидалось сообщение LOGIN"));
            return false;
        }

        this.nickname = loginMsg.getFrom();
        if (nickname == null || nickname.isBlank()) {
            log.info("Клиент {} попытался войти с пустым ником",
                    socket.getRemoteSocketAddress());
            connection.send(ChatProtocol.system("Пустой ник недопустим"));
            return false;
        }

        boolean ok = server.registerClient(nickname, this);
        if (!ok) {
            log.info("Попытка входа с уже занятым ником '{}' от клиента {}",
                    nickname, socket.getRemoteSocketAddress());
            connection.send(ChatProtocol.system(
                    "Ник '" + nickname + "' уже занят. Попробуйте другое имя."));
            return false;
        }

        log.info("Клиент '{}' успешно вошёл в чат с адреса {}",
                nickname, socket.getRemoteSocketAddress());

        connection.send(ChatProtocol.system("Вы вошли в чат как " + nickname));
        return true;
    }

    private void listenMessages() throws IOException {
        ChatMessage msg;
        while ((msg = connection.receive()) != null) {
            if (msg.getType() == MessageType.CHAT) {
                msg.setFrom(nickname);
                msg.setTimestamp(ChatProtocol.now());
                server.broadcast(msg);
            } else {
                log.debug("Получено сообщение типа {} от клиента '{}'", msg.getType(), nickname);
            }
        }

        log.info("Клиент '{}' ({}) завершил соединение",
                nickname, socket.getRemoteSocketAddress());
    }

    private void cleanup() {
        server.removeClient(nickname, this);

        try {
            if (connection != null) {
                connection.close();
            }
        } catch (IOException e) {
            log.debug("Ошибка при закрытии соединения с клиентом '{}': {}",
                    nickname, e.getMessage());
        }
    }

}
