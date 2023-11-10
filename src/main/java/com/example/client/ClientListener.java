package com.example.client;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * A listener thread for a client connection. Continuously checks for new messages from a server
 * and sends them to the {@link Client}.
 *
 * @author Sigve Bjørkedal
 */
public class ClientListener extends Thread {
	Client client;
	BufferedReader messageStream;

	/**
	 * Creates a new listening thread based on a client connection.
	 *
	 * @param client the client this connection belongs to
	 * @param messageStream the input the listener should listen to
	 */
	public ClientListener(Client client, BufferedReader messageStream) {
		this.client = client;
		this.messageStream = messageStream;
	}

	@Override
	public void run() {
		String incomingMessage = null;

		do {
			try {
				incomingMessage = messageStream.readLine();
				handleIncomingMessage(incomingMessage);
			} catch (IOException e) {
				System.err.println("Failed to receive input: " + e.getMessage());
			}
		} while (incomingMessage == null);

		closeInput();
	}

	/**
	 * Registers an incoming message to the client.
	 *
	 * @param message message to register
	 */
	private synchronized void handleIncomingMessage(String message) {
		this.client.registerIncomingMessage(message);
	}

	/**
	 * Attempts to close the connection.
	 */
	private void closeInput() {
		try {
			this.messageStream.close();
		} catch (IOException e) {
			System.err.println("Failed to close input stream: " + e.getMessage());
		}
	}
}
