package no.ntnu.sigve.sockets;

import java.io.IOException;
import no.ntnu.sigve.communication.Message;

/**
 * Interface representing a client socket.
 */
public interface ClientSocket {
	/**
	 * Sends a message through this socket.
	 *
	 * @param message the message to semd
	 */
	public void sendMessage(Message<?> message) throws IOException;

	/**
	 * Waits for a message to be received from this socket.
	 *
	 * @return the message received from the socket
	 */
	public Message<?> receiveMessage() throws IOException, ClassNotFoundException;

	/**
	 * Attempts to close this socket.
	 */
	public void close() throws IOException;

	/**
	 * Attempts to connect the socket to the server.
	 *
	 * @throws IOException if connecting to the server fails
	 */
	public void connect() throws IOException;

}