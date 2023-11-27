package no.ntnu.sigve.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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


	private final LinkedList<Message<?>> incomingMessages;
	private final ObjectOutputStream output;
	private final Socket socket;
	private final List<MessageObserver> observers;
	private final UUID sessionId;

	/**
	 * Creates a new client connection to a server.
	 *
	 * @param address the address of the server to connect to
	 * @param port    the port of the server to connect to
	 * @throws IOException if connecting to the server fails
	 */
	public Client(String address, int port) throws IOException {
		this.incomingMessages = new LinkedList<>();
		this.socket = new Socket(address, port);
		this.observers = new ArrayList<>();

		ObjectInputStream socketResponseStream =
				new ObjectInputStream(this.socket.getInputStream());
		this.output = new ObjectOutputStream(this.socket.getOutputStream());

		UUID uuid = null;
		try {
			UuidMessage uuidMessage = (UuidMessage) socketResponseStream.readObject();
			if (uuidMessage != null) {
				uuid = uuidMessage.getPayload();
			}
		} catch (ClassNotFoundException cnfe) {
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
	 * Closes the connection to the server.
	 */
	public void close() {
		try {
			socket.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * Gets the session ID.
	 *
	 * @return The session ID
	 */
	public UUID getSessionId() {
		return sessionId;
	}

	/**
	 * Sends a message to the server.
	 *
	 * @param message to send to the server.
	 */
	public void sendOutgoingMessage(Message<?> message) {
		try {
			this.output.writeObject(message);
		} catch (IOException ioe) {
			//TODO: Possibly do something lol
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
	 * Removes an observer from the client. The observer will no longer receive message notifications.
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
