package no.ntnu.sigve.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;
import no.ntnu.sigve.communication.Message;
import no.ntnu.sigve.communication.Protocol;
import no.ntnu.sigve.communication.ProtocolUser;
import no.ntnu.sigve.communication.UuidMessage;

/**
 * A client connection to a server. Capable of continuously reading information from the client and
 * sending messages.
 *
 * @author Sigve Bjørkedal
 * @see Client#sendOutgoingMessage(Message) sendOutgoingMessage
 * @see Client#nextIncomingMessage() nextIncomingMessage
 */
public class Client extends ProtocolUser {
	private static final String NOT_CONNECTED_MESSAGE = "Client is not connected";

	private final String address;
	private final int port;

	private ObjectOutputStream output;
	private Socket socket;
	private UUID sessionId;

	/**
	 * Creates a new client connection to a server.
	 *
	 * @param address the address of the server to connect to
	 * @param port    the port of the server to connect to
	 */
	public Client(String address, int port, Protocol<Client> protocol) {
		super(protocol);
		this.address = address;
		this.port = port;
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
		onClientConnected();
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
	 * Notifies the connected protocol that a new message has been received.
	 *
	 * @param message the received message object
	 */
	public void registerIncomingMessage(Message<?> message) {
		this.onMessageReceived(message);
	}

	/**
	 * Notifies the connected protocol that the client has connected to a server and received a
	 * session id.
	 */
	public void onClientConnected() {
		this.onClientConnect(this.sessionId);
	}

	/**
	 * Notifies the connected protocol that the client has disconnected from the server.
	 */
	public void onClientDisconnected() {
		this.onClientDisconnect(this.sessionId);
	}
}
