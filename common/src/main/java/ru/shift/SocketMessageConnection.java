package ru.shift;


import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SocketMessageConnection implements MessageConnection {

    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private final MessageSerializer serializer;

    public SocketMessageConnection(Socket socket, MessageSerializer serializer) throws IOException {
        this.socket = socket;
        this.serializer = serializer;
        this.in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.out = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8),
                true);
    }

    @Override
    public void send(ChatMessage message) {
        String data = serializer.serialize(message);
        out.println(data);
    }

    @Override
    public ChatMessage receive() throws IOException {
        String line = in.readLine();
        if (line == null) {
            return null;
        }
        return serializer.deserialize(line);
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
