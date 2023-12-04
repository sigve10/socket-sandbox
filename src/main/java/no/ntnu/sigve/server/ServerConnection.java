package no.ntnu.sigve.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SequenceWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.UUID;
import no.ntnu.sigve.communication.Message;

/**
 * A connection from a {@link Server} to one individual client. Handles the connection independent
 * of other connected clients.
 */
public class ServerConnection extends Thread {
	private final Socket clientSocket;
	private final InputStream input;
	private final SequenceWriter replyOutput;
	private final Server server;
	private final UUID clientUuid;
	private final ObjectMapper json;
	private final ObjectReader jsonReader;

	/**
	 * Creates a new threaded connection from a {@link Server} to a
	 * {@link no.ntnu.sigve.client.Client}.
	 *
	 * @param server       the connected server
	 * @param clientSocket the socket connection belonging to the client.
	 * @param clientUuid   the UUID of the connected client.
	 * @throws IOException if a connection could not be established.
	 */
	public ServerConnection(
			Server server,
			Socket clientSocket,
			UUID clientUuid
	) throws IOException {
		this.clientSocket = clientSocket;
		this.server = server;
		this.clientUuid = clientUuid;

		this.json = new ObjectMapper();
		this.jsonReader = json.readerFor(json.getTypeFactory().constructType(new TypeReference<Message<?>>(){}));
		this.replyOutput = json.writer().writeValues(clientSocket.getOutputStream());
		this.input = clientSocket.getInputStream();
	}

	@Override
	public void run() {
		boolean keepRunning = true;
		while (keepRunning) {
			keepRunning = readClientRequest();
		}
		this.server.removeExistingConnection(this.clientUuid);
		closeConnection();
	}

	/**
	 * Reads and processes the next message from the client. If a special
	 * command (example "SEND") is detected, performs the associated action
	 * (such as broadcasting a message to all clients). Otherwise, forwards
	 * the message to the protocol for further processing.
	 *
	 * @return boolean indicating whether to continue running. Returns false
	 * 		if the end of the stream is reached or an IOException occurs, signaling
	 * 		the server connection to shut down.
	 */
	private boolean readClientRequest() {
		MappingIterator<Message<?>> messages = null;
		boolean retval = false;

		try {
			messages = jsonReader.readValues(input);
			//message = json.readValue((String) input.readObject(), new TypeReference<>(){});
		} catch (ClassCastException e) {
			System.err.println("Discarding un-castable request from client. " + e.getMessage());
			retval = true;
		} catch (IOException e) {
			System.err.println("Could not handle request. " + e.getMessage());
		}

		if (messages != null) {
			messages.forEachRemaining(message -> {
				message.assignSource(clientUuid);
				server.registerIncomingMessage(message);
			});
			retval = true;
		}

		return retval;
	}


	/**
	 * Sends a message to the client.
	 *
	 * @param message the message to send
	 */
	public void sendMessage(Message<?> message) {
		try {
			replyOutput.write(message, json.getTypeFactory().constructType(new TypeReference<Message<?>>(){}));
		} catch (JsonProcessingException jpe) {
			System.err.printf(
					"Could not serialize message%nMessage type: %s%nPayload type: %s%nMessage: %s%n",
					message.getClass(), message.getPayload().getClass(), message
			);
			jpe.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Attempts to close the connection.
	 */
	public void close() {
		try {
			this.clientSocket.close();
			input.close();
		} catch (IOException e) {
			System.err.println("Could not close socket. " + e.getMessage());
		}
	}


	/**
	 * Closes the connection by shutting down the input stream, output stream,
	 * and the socket. Ensures that all resources are properly released to
	 * prevent resource leaks. This method should be called when the connection
	 * is no longer needed or when an exception occurs.
	 */
	private void closeConnection() {
		try {
			if (input != null) {
				input.close();
			}
		} catch (IOException e) {
			System.err.println("Error closing input stream: " + e.getMessage());
		}

		try {
			if (replyOutput != null) {
				replyOutput.close();
			}
		} catch (IOException e) {
			System.err.println("Error closing output stream: " + e.getMessage());
		}

		try {
			if (clientSocket != null && !clientSocket.isClosed()) {
				clientSocket.close();
			}
		} catch (IOException e) {
			System.err.println("Error closing socket: " + e.getMessage());
		}
	}

}
