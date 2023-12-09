package no.ntnu.sigve.sockets;

import java.io.IOException;
import no.ntnu.sigve.communication.Message;

/**
 * Interface representing a client socket.
 */
public interface ClientSocket {
	/**
	 * Connects the socket to a server.
	 *
	 * @throws IOException if the connection fails to be established
	 */
	void connect() throws IOException;

	/**
	 * Attempts to send a message through this socket.
	 *
	 * @param message the message to be sent
	 * @throws IOException if the message fails to send
	 */
	void sendMessage(Message<?> message) throws IOException;
	Message<?> receiveMessage() throws IOException, ClassNotFoundException;
	boolean isClosed();
	void close() throws IOException;
}