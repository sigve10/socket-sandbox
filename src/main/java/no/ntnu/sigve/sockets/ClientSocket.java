package no.ntnu.sigve.sockets;

import java.io.IOException;

import no.ntnu.sigve.communication.Message;

public interface ClientSocket {
    void connect() throws IOException;
    void sendMessage(Message<?> message) throws IOException;
    Message<?> receiveMessage() throws IOException, ClassNotFoundException;
    boolean isClosed();
    void close() throws IOException;
}