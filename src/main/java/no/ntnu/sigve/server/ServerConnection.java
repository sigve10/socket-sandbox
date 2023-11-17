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

	@Override
	public void run() {
		while (true) {
			readClientRequest();
		}
	}

	/**
	 * Attempts to read the next message from the client. The message is then sent to this
	 * connection's {@link Protocol} for processing.
	 */
	private void readClientRequest() {
		try {
			String rawMessage = input.readLine();
			System.out.println(" >>> " + rawMessage);
			this.protocol.receiveMessage(rawMessage, clientSocket.getInetAddress());
		} catch (IOException e) {
			System.err.println("Could not handle request. " + e.getMessage());
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
}
