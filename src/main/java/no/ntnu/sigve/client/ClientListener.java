package no.ntnu.sigve.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import no.ntnu.sigve.communication.Message;
import no.ntnu.sigve.sockets.ClientSocket;

/**
 * A separate thread from a client which is responsible for actively listening for new messages from
 * the server.
 */
public class ClientListener extends Thread {

	Client client;
	ClientSocket socket;

	public ClientListener(Client client, ClientSocket socket) {
		this.client = client;
		this.socket = socket;
	}

	/**
	 * Continuously listens for messages from the server and handles them.
	 * Notifies the client upon disconnection or when an exception occurs.
	 */
	@Override
	public void run() {
		try {
			Message<?> incomingMessage;
			do {
				incomingMessage = socket.receiveMessage();
			} while (incomingMessage != null);
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
		e.printStackTrace();
	}

	private void closeInput() {
		try {
			if (this.socket != null) {
				this.socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
