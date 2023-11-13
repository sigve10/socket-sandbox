package com.example.client;

import java.io.BufferedReader;
import java.io.IOException;

public class ClientListener extends Thread {

	Client client;
	BufferedReader messageStream;

	public ClientListener(Client client, BufferedReader messageStream) {
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
		    String incomingMessage;
			while ((incomingMessage = messageStream.readLine()) != null) {
				handleIncomingMessage(incomingMessage);
			}
			client.notifyDisconnection();
		} catch (IOException e) {
			handleException(e);
		}     finally {
			closeInput();
		}
	}

	/**
	 * Handles incoming messages by registering them with the client.
	 *
	 * @param message The incoming message to handle.
	 */

	private synchronized void handleIncomingMessage(String message) {
		this.client.registerIncomingMessage(message);
	}

	/**
	 * Handles exceptions by logging the error and notifying the client.
	 *
	 * @param e The exception to handle.
	 */
	private void handleException(Exception e) {
		System.err.println("Error in ClientListener: " + e.getMessage());
		client.notifyException(e);
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
