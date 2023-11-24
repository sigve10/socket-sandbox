package no.ntnu.sigve.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.UUID;
import no.ntnu.sigve.communication.Message;

/**
 * A connection from a {@link Server} to one individual client. Handles the connection independent
 * of other connected clients.
 */
public class ServerConnection extends Thread {
	private final Socket clientSocket;
	private final ObjectInputStream input;
	private final ObjectOutputStream replyOutput;
	private final Protocol protocol;
	private final UUID address;

	/**
	 * Creates a new threaded connection from a {@link Server} to a
	 * {@link no.ntnu.sigve.client.Client}.
	 *
	 * @param protocol     the protocol on which the connection runs.
	 * @param clientSocket the socket connection belonging to the client.
	 * @param address      the UUID
	 * @throws IOException if a connection could not be established.
	 */
	public ServerConnection(
			Protocol protocol,
			Socket clientSocket,
			UUID address
	) throws IOException {
		this.clientSocket = clientSocket;
		this.protocol = protocol;
		this.address = address;

		replyOutput = new ObjectOutputStream(clientSocket.getOutputStream());
		input = new ObjectInputStream(clientSocket.getInputStream());
	}

	@Override
	public void run() {
		boolean keepRunning = true;
		while (keepRunning) {
			keepRunning = readClientRequest();
		}
		closeConnection();
	}

	/**
	 * Reads and processes the next message from the client. If a special
	 * command (example "SEND") is detected, performs the associated action
	 * (such as broadcasting a message to all clients). Otherwise, forwards
	 * the message to the protocol for further processing.
	 *
	 * @return boolean indicating whether to continue running. Returns false
	 *         if the end of the stream is reached or an IOException occurs, signaling
	 *         the server connection to shut down.
	 */
	private boolean readClientRequest() {
		Message<? extends Serializable> message = null;
		boolean retval = false;

		try {
			message = (Message<? extends Serializable>) input.readObject();
		} catch (ClassCastException | ClassNotFoundException e) {
			System.err.println("Discarding uncastable request from client. " + e.getMessage());
			retval = true;
		} catch (IOException e) {
			System.err.println("Could not handle request. " + e.getMessage());
		}

		if (message != null) {
			message.assignSource(this.address);
			this.protocol.receiveMessage(message);
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
			replyOutput.writeObject(message);
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
