package com.example.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;

/**
 * A client connection to a server. Capable of continuously reading information from the client and
 * sending messages.
 * 
 * @see Client#sendOutgoingMessage(String) sendOutgoingMessage
 * @see Client#nextIncomingMessage() nextIncomingMessage
 * 
 * @author Sigve Bjørkedal
 */
public class Client {
	private LinkedList<String> incomingMessages;
	private PrintWriter output;
	private Socket socket;

	/**
	 * Creates a new client connection to a server.
	 * 
	 * @param address the address of the server to connect to
	 * @param port the port of the server to connect to
	 * @throws IOException if connecting to the server fails
	 */
	public Client(String address, int port) throws IOException {
		this.incomingMessages = new LinkedList<>();
		this.socket =  new Socket(address, port);

		BufferedReader socketResponseStream = new BufferedReader(
			new InputStreamReader(
				this.socket.getInputStream()));

		this.output = new PrintWriter(this.socket.getOutputStream(), true);

		new ClientListener(this, socketResponseStream).start();
	}

	/**
	 * Sends a message to the server.
	 *
	 * @param message message to send to the server.
	 */
	public void sendOutgoingMessage(String message) {
		this.output.println(message);
	}

	/**
	 * Attempts to retrieve the earliest received message from the server.
	 * 
	 * @return the earliest received message, or null if it does not exist.
	 */
	public String nextIncomingMessage() {
		String retval = null;

		if (this.incomingMessages.peek() != null) {
			retval = this.incomingMessages.getFirst();
		}

		return retval;
	}

	/**
	 * Registers a new message to the client. Can be read through {@link Client#nextIncomingMessage
	 * nextIncomingMessage}.
	 *
	 * @param message the message to register.
	 */
	public void registerIncomingMessage(String message) {
		this.incomingMessages.add(message);
	}
}
