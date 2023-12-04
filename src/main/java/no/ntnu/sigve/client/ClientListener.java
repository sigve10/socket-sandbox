package no.ntnu.sigve.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import no.ntnu.sigve.communication.Message;

/**
 * A separate thread from a client which is responsible for actively listening for new messages from
 * the server.
 */
public class ClientListener extends Thread {

	Client client;
	ObjectInputStream messageStream;

	/**
	 * Creates a new client listener.
	 *
	 * @param client the client this listener belongs to
	 * @param messageStream the socket input stream this listener should listen to
	 */
	public ClientListener(Client client, ObjectInputStream messageStream) {
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
			Message<?> incomingMessage;
			while ((incomingMessage = (Message<?>) messageStream.readObject()) != null) {
				handleIncomingMessage(incomingMessage);
			}
		} catch (IOException | ClassNotFoundException e) {
			handleException(e);
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

	private synchronized void handleIncomingMessage(Message<?> message) {
		this.client.registerIncomingMessage(message);
	}

	/**
	 * Handles exceptions by logging the error and notifying the client.
	 *
	 * @param e The exception to handle.
	 */
	private void handleException(Exception e) {
		System.err.println("Error in ClientListener: " + e.getMessage());
	}

	private void closeInput() {
		try {
			if (this.messageStream != null) {
				this.messageStream.close();
			}
		} catch (IOException e) {
			System.err.println("Failed to close input stream: " + e.getMessage());
		}
	}
}
