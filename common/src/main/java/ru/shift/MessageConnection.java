package ru.shift;

import java.io.Closeable;
import java.io.IOException;

public interface MessageConnection extends Closeable {

    void send(ChatMessage message) throws IOException;
    ChatMessage receive() throws IOException;

}
