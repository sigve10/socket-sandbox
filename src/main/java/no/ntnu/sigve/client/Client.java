package no.ntnu.sigve.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
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
 */
public class Client implements ProtocolUser {
	private static final String NOT_CONNECTED_MESSAGE = "Client is not connected";

	private final String address;
	private final int port;
	private final Protocol<Client> protocol;
	private final ObjectMapper json;

	private SequenceWriter output;
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
		this.json = new ObjectMapper();
	}

	/**
	 * Tries to connect to the server.
	 *
	 * @throws IOException If connecting to the server fails
	 */
	public void connect() throws IOException {
		this.socket = new Socket(address, port);

		new ClientListener(this, socket.getInputStream()).start();
		this.output = json.writer().writeValues(this.socket.getOutputStream());
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
			output.write(message, json.getTypeFactory().constructType(new TypeReference<Message<?>>(){}));
		} catch (JsonProcessingException jpe) {
			System.err.printf(
					"Could not serialize message%nMessage type: %s%nPayload type: %s%nMessage: %s%n",
					message.getClass(), message.getPayload().getClass(), message
			);
			jpe.printStackTrace();
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
		if (message instanceof UuidMessage uuidMessage) {
			this.sessionId = uuidMessage.getPayload();
			System.out.println("Received session ID: " + sessionId);
		}
		this.protocol.receiveMessage(this, message);
	}

	/**
	 * Notifies the connected protocol that the client has connected to a server and received a
	 * session id.
	 */
	public void onClientConnected() {
		this.protocol.onClientConnect(this, this.sessionId);
	}

	/**
	 * Notifies the connected protocol that the client has disconnected from the server.
	 */
	public void onClientDisconnected() {
		this.protocol.onClientDisconnect(this, this.sessionId);
	}
}
