package no.ntnu.sigve.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.UUID;
import no.ntnu.sigve.communication.Message;
import no.ntnu.sigve.communication.Protocol;
import no.ntnu.sigve.communication.ProtocolUser;
import no.ntnu.sigve.communication.UnknownMessage;
import no.ntnu.sigve.communication.UuidMessage;

/**
 * A client connection to a server. Capable of continuously reading information from the client and
 * sending messages.
 *
 * @author Sigve Bjørkedal
 * @see Client#sendOutgoingMessage(Message) sendOutgoingMessage
 */
public class Client implements ProtocolUser {
	private static final String NOT_CONNECTED_MESSAGE = "Client is not connected";

	private final String address;
	private final int port;
	private final Protocol<Client> protocol;

	private DataOutputStream output;
	private Socket socket;
	private UUID sessionId;

	/**
	 * Creates a new client connection to a server.
	 *
	 * @param address  the address of the server to connect to
	 * @param port     the port of the server to connect to
	 * @param protocol the protocol by which the client will interpret messages.
	 */
	public Client(String address, int port, Protocol<Client> protocol) {
		this.address = address;
		this.port = port;
		this.protocol = protocol;
	}

	/**
	 * Tries to connect to the server.
	 *
	 * @throws IOException If connecting to the server fails
	 */
	public void connect() throws IOException {
		this.socket = new Socket(address, port);

		DataInputStream socketResponseStream = new DataInputStream(this.socket.getInputStream());
		this.output = new DataOutputStream(this.socket.getOutputStream());

		UUID uuid = null;
		String[] serializedParams = socketResponseStream.readUTF().split("\\|");
		if (UuidMessage.TYPE_IDENTIFIER.equals(serializedParams[2])) {
			uuid = UUID.fromString(serializedParams[3]);
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
	public void sendOutgoingMessage(Message message) {
		if (socket == null) {
			throw new IllegalStateException(NOT_CONNECTED_MESSAGE);
		}
		try {
			this.output.writeUTF(message.getSerialized());
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
	public void registerIncomingMessage(UnknownMessage message) {
		protocol.receiveMessage(this, protocol.resolveMessage(this, message));
	}

	/**
	 * Notifies the connected protocol that the client has connected to a server and received a
	 * session id.
	 */
	public void onClientConnected() {
		protocol.onClientConnect(this, this.sessionId);
	}

	/**
	 * Notifies the connected protocol that the client has disconnected from the server.
	 */
	public void onClientDisconnected() {
		protocol.onClientDisconnect(this, this.sessionId);
	}
}
