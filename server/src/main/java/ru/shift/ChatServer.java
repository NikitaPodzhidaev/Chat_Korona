package ru.shift;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatServer {

    private static final String LOG_CHAT_TEMPLATE   = "[ЧАТ] [{}] {}: {}";
    private static final String LOG_SYSTEM_TEMPLATE = "[СИСТЕМА] [{}] {}";
    private static final String LOG_USERS_TEMPLATE  = "[ПОЛЬЗОВАТЕЛИ] онлайн: {}";
    private static final String LOG_LOGIN_TEMPLATE  = "[ЛОГИН] запрос входа от: {}";

    private static final Logger log = LoggerFactory.getLogger(ChatServer.class);

    private final int port;

    private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();

    private final ExecutorService clientExecutor;

    public ChatServer(int port) {
        this.port = port;
        this.clientExecutor = Executors.newCachedThreadPool();
    }

    public void start() throws IOException {
        log.info("Сервер запускается на порту {}", port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket socket = serverSocket.accept();
                log.info("Новое подключение клиента: {}", socket.getRemoteSocketAddress());

                ClientHandler handler = new ClientHandler(socket, this);
                clientExecutor.submit(handler);
            }
        } finally {
            shutdownExecutor();
        }
    }

    private void shutdownExecutor() {
        clientExecutor.shutdown();
        try {
            if (!clientExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("Пул потоков не завершился вовремя, принудительное завершение");
                clientExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.warn("Ожидание завершения пула потоков было прервано", e);
            clientExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public boolean registerClient(String nickname, ClientHandler handler) {
        ClientHandler existing = clients.putIfAbsent(nickname, handler);
        return existing == null;
    }

    public void removeClient(String nickname, ClientHandler handler) {
        if (nickname == null) {
            return;
        }

        boolean removed = clients.remove(nickname, handler);
        if (removed) {
            log.info("Клиент '{}' вышел из чата", nickname);
            broadcast(ChatProtocol.clientLeft(nickname));
            broadcastUserList();
        }
    }


    public void broadcast(ChatMessage msg) {
        for (ClientHandler client : clients.values()) {
            client.send(msg);
        }
        logMessage(msg);
    }

    public void broadcastSystem(String text) {
        broadcast(ChatProtocol.system(text));
    }

    public void broadcastUserList() {
        List<String> users = List.copyOf(clients.keySet());
        broadcast(ChatProtocol.users(users));
    }

    private void logMessage(ChatMessage msg) {
        if (msg == null || msg.getType() == null) {
            return;
        }

        switch (msg.getType()) {
            case CHAT -> log.info(
                    LOG_CHAT_TEMPLATE,
                    msg.getTimestamp(),
                    msg.getFrom(),
                    msg.getText()
            );
            case SYSTEM -> log.info(
                    LOG_SYSTEM_TEMPLATE,
                    msg.getTimestamp(),
                    msg.getText()
            );
            case USERS -> log.info(
                    LOG_USERS_TEMPLATE,
                    msg.getUsers()
            );
            case LOGIN -> log.info(
                    LOG_LOGIN_TEMPLATE,
                    msg.getFrom()
            );
        }
    }
}
