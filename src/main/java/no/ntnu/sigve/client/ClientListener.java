package no.ntnu.sigve.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import java.io.IOException;
import java.io.InputStream;
import no.ntnu.sigve.communication.Message;

public class ClientListener extends Thread {

	private final Client client;
	private final InputStream messageStream;
	private final ObjectMapper json;
	private final ObjectReader jsonReader;

	public ClientListener(Client client, InputStream messageStream) {
		this.client = client;
		this.messageStream = messageStream;
		this.json = new ObjectMapper();
		this.jsonReader = json.readerFor(json.getTypeFactory().constructType(new TypeReference<Message<?>>(){}));
	}

	/**
	 * Continuously listens for messages from the server and handles them.
	 * Notifies the client upon disconnection or when an exception occurs.
	 */
	@Override
	public void run() {
		try {
			MappingIterator<Message<?>> messages;
			while ((messages = jsonReader.readValues(messageStream)) != null) {
				messages.forEachRemaining(this::handleIncomingMessage);
			}
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

	private synchronized void handleIncomingMessage(Message<?> message) {
		this.client.registerIncomingMessage(message);
	}

	/**
	 * Handles exceptions by logging the error and notifying the client.
	 *
	 * @param e The exception to handle.
	 */
	private void handleException(Exception e) {
		System.err.println("Error in ClientListener: ");
		e.printStackTrace();
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
