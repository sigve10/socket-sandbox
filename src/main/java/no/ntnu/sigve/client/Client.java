package no.ntnu.sigve.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import no.ntnu.sigve.communication.Message;
import no.ntnu.sigve.communication.UuidMessage;

/**
 * A client connection to a server. Capable of continuously reading information from the client and
 * sending messages.
 *
 * @author Sigve Bjørkedal
 * @see Client#sendOutgoingMessage(Message) sendOutgoingMessage
 * @see Client#nextIncomingMessage() nextIncomingMessage
 */
public class Client {
	private static final String NOT_CONNECTED_MESSAGE = "Client is not connected";

	private final String address;
	private final int port;
	private final LinkedList<Message<?>> incomingMessages;
	private final List<MessageObserver> observers;

	private ObjectOutputStream output;
	private Socket socket;
	private UUID sessionId;

	/**
	 * Creates a new client connection to a server.
	 *
	 * @param address the address of the server to connect to
	 * @param port    the port of the server to connect to
	 */
	public Client(String address, int port) {
		this.address = address;
		this.port = port;
		this.incomingMessages = new LinkedList<>();
		this.observers = new ArrayList<>();
	}

	/**
	 * Tries to connect to the server.
	 *
	 * @throws IOException If connecting to the server fails
	 */
	public void connect() throws IOException {
		this.socket = new Socket(address, port);

		ObjectInputStream socketResponseStream =
				new ObjectInputStream(this.socket.getInputStream());
		this.output = new ObjectOutputStream(this.socket.getOutputStream());

		UUID uuid = null;
		try {
			UuidMessage uuidMessage = (UuidMessage) socketResponseStream.readObject();
			if (uuidMessage != null) {
				uuid = uuidMessage.getPayload();
			}
		} catch (ClassNotFoundException | ClassCastException e) {
			//Do nothing
		}
		if (uuid != null) {
			this.sessionId = uuid;
			System.out.println("Received session ID: " + this.sessionId);
		} else {
			throw new IllegalStateException("Session ID was not received properly.");
		}

		new ClientListener(this, socketResponseStream).start();
	}

	/**
	 * Gets the session ID.
	 *
	 * @return The session ID
	 */
	public UUID getSessionId() {
		if (socket == null) {
			throw new IllegalStateException(NOT_CONNECTED_MESSAGE);
		}
		return sessionId;
	}

	/**
	 * Sends a message to the server.
	 *
	 * @param message to send to the server.
	 */
	public void sendOutgoingMessage(Message<?> message) {
		if (socket == null) {
			throw new IllegalStateException(NOT_CONNECTED_MESSAGE);
		}
		try {
			this.output.writeObject(message);
		} catch (IOException ioe) {
			System.err.println("Could not send outgoing message. Here's the stacktrace:");
			ioe.printStackTrace();
		}
	}

	/**
	 * Attempts to retrieve the earliest received message from the server.
	 *
	 * @return the earliest received message, or null if it does not exist.
	 */
	public Message<?> nextIncomingMessage() {
		Message<?> retval = null;

		if (this.incomingMessages.peek() != null) {
			retval = this.incomingMessages.removeFirst();
		}

		return retval;
	}

	/**
	 * Registers a new message to the client. Can be read through {@link Client#nextIncomingMessage
	 * nextIncomingMessage}.
	 *
	 * @param message the message to register.
	 */
	public void registerIncomingMessage(Message<?> message) {
		this.incomingMessages.add(message);
		notifyObservers(message);
	}

	/**
	 * Adds an observer to the client. The observer will be notified of new messages.
	 *
	 * @param observer The observer to add.
	 */
	public void addObserver(MessageObserver observer) {
		this.observers.add(observer);
	}

	/**
	 * Removes an observer from the client.
	 * The observer will no longer receive message notifications.
	 *
	 * @param observer The observer to remove.
	 */
	public void removeObserver(MessageObserver observer) {
		this.observers.remove(observer);
	}

	/**
	 * Notifies all registered observers with the given message. This method is called
	 * when a new message is received and needs to be communicated to all observers.
	 *
	 * @param message The message to be sent to the observers.
	 */
	private void notifyObservers(Message<?> message) {
		for (MessageObserver observer : this.observers) {
			observer.update(message);
		}
	}
}
