package no.ntnu.sigve.client;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import no.ntnu.sigve.communication.UnknownMessage;

/**
 * A separate thread from a client which is responsible for actively listening for new messages from
 * the server.
 */
public class ClientListener extends Thread {
    private final Client client;
    private final DataInputStream messageStream;

    /**
     * Creates a new client listener.
     *
     * @param client        the client this listener belongs to
     * @param messageStream the socket input stream this listener should listen to
     */
    public ClientListener(Client client, DataInputStream messageStream) {
        this.client = client;
        this.messageStream = messageStream;
    }

    /**
     * Continuously listens for messages from the server and handles them.
     * Notifies the client upon disconnection or when an exception occurs.
     */
    @Override
    public void run() {
        try {
            UnknownMessage incomingMessage;
            while ((incomingMessage = UnknownMessage.fromString(messageStream.readUTF())) != null) {
                handleIncomingMessage(incomingMessage);
            }
        } catch (EOFException eofe) {
            // Stream closed & there's nothing more to read, nothing needs to be done
        } catch (IOException ioe) {
            handleException(ioe);
        } finally {
            closeInput();
            this.client.onClientDisconnected();
        }
    }

    /**
     * Handles incoming messages by registering them with the client.
     *
     * @param message The incoming message to handle.
     */

    private synchronized void handleIncomingMessage(UnknownMessage message) {
        client.registerIncomingMessage(message);
    }

    /**
     * Handles exceptions by logging the error and notifying the client.
     *
     * @param e The exception to handle.
     */
    private void handleException(Exception e) {
        e.printStackTrace();
    }

    private void closeInput() {
        try {
            if (this.messageStream != null) {
                this.messageStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
