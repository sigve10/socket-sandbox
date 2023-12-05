package no.ntnu.sigve.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.UUID;
import no.ntnu.sigve.communication.Message;
import no.ntnu.sigve.communication.UnknownMessage;

/**
 * A connection from a {@link Server} to one individual client. Handles the connection independent
 * of other connected clients.
 */
public class ServerConnection extends Thread {
	private final Socket clientSocket;
	private final DataInputStream input;
	private final DataOutputStream replyOutput;
	private final Server server;
	private final UUID clientUuid;

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

		replyOutput = new DataOutputStream(clientSocket.getOutputStream());
		input = new DataInputStream(clientSocket.getInputStream());
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
	 *     if the end of the stream is reached or an IOException occurs, signaling
	 *     the server connection to shut down.
	 */
	private boolean readClientRequest() {
		UnknownMessage message = null;
		boolean retval = false;

		try {
			message = UnknownMessage.fromString(input.readUTF());
		} catch (IOException e) {
			System.err.println("(Server) Exception while listening for messages: " + e.getMessage());
		}

		if (message != null) {
			message.assignSource(clientUuid);
			server.registerIncomingMessage(message);
			retval = true;
		}

		return retval;
	}

	/**
	 * Sends a message to the client.
	 *
	 * @param message the message to send
	 */
	public void sendMessage(Message message) {
		try {
			replyOutput.writeUTF(message.getSerialized());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Closes the connection by shutting down the input stream, output stream,
	 * and the socket. Ensures that all resources are properly released to
	 * prevent resource leaks. This method should be called when the connection
	 * is no longer needed or when an exception occurs.
	 */
	public void closeConnection() {
		try {
			if (input != null) {
				input.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			if (replyOutput != null) {
				replyOutput.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			if (clientSocket != null && !clientSocket.isClosed()) {
				clientSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
