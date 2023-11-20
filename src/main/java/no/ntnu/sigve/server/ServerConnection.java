package no.ntnu.sigve.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * A connection from a {@link Server} to one individual client. Handles the connection independent
 * of other connected clients.
 */
public class ServerConnection extends Thread {
	private Socket clientSocket;
	private BufferedReader input;
	private PrintWriter replyOutput;
	private Protocol protocol;

	/**
	 * Creates a new threaded connection from a {@link Server} to a 
	 * {@link com.example.client.Client Client}.
	 *
	 * @param protocol the protocol on which the connection runs.
	 * @param clientSocket the socket connection belonging to the client.
	 * @throws IOException if a connection could not be established.
	 */
	public ServerConnection(Protocol protocol, Socket clientSocket) throws IOException {
		this.clientSocket = clientSocket;
		this.protocol = protocol;

		input = new BufferedReader(
			new InputStreamReader(
				clientSocket.getInputStream()
			)
		);

		replyOutput = new PrintWriter(
			clientSocket.getOutputStream(), true
		);
	}

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
	 * if the end of the stream is reached or an IOException occurs, signaling
	 * the server connection to shut down.
	 */
	private boolean readClientRequest() {
		try {
			String rawMessage = input.readLine();
			if (rawMessage != null) {
				System.out.println(" >>> " + rawMessage);
				if ("SEND".equalsIgnoreCase(rawMessage)) {
					Server.getInstance().broadcast("Message from server to all clients");
				} else {
					this.protocol.receiveMessage(rawMessage, clientSocket.getInetAddress());
				}
				return true;
			} else {
				return false;
			}
		} catch (IOException e) {
			System.err.println("Could not handle request: " + e.getMessage());
			return false;
		}
	}



	/**
	 * Sends a message to the client.
	 *
	 * @param message the message to send
	 */
	public void sendMessage(String message) {
		replyOutput.println(message);
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
	
		if (replyOutput != null) {
			replyOutput.close();
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
