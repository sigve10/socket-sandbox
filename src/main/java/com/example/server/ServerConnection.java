package com.example.server;

import com.example.server.commands.CommandSet;
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
	private CommandSet commandSet;
	private PrintWriter replyOutput;

	/**
	 * Creates a new connection.
	 *
	 * @param commandSet the set of commands this connection should be able to interpret
	 * @param clientSocket the socket this connection should listen to
	 * @throws IOException if the connection fails
	 */
	public ServerConnection(CommandSet commandSet, Socket clientSocket) throws IOException {
		this.clientSocket = clientSocket;
		this.commandSet = commandSet;

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
		String response;

		do {
			response = readClientRequest();
			if (response != null) {
				System.out.println("Server: Received message \"" + response + "\"");
				sendMessage(response);
			}
		} while (response != null && !response.equalsIgnoreCase("Disconnect"));

		this.close();
	}

	/**
	 * Attempts to read and interpret a request from the client, as well as run it.
	 *
	 * @return a string response to the command, or an invalid command message if the command does
	 *     not exist.
	 */
	private String readClientRequest() {
		String message = null;
		
		try {
			String rawMessage = input.readLine();
			System.out.println(" >>> " + rawMessage);

			message = this.commandSet.tryToExecuteCommand(rawMessage);
		} catch (IOException e) {
			System.err.println("Could not handle request. " + e.getMessage());
		}
		
		return message;
	}

	/**
	 * Send a reply back to the client.
	 *
	 * @param message The message that will be sent to the client
	 */
	public void sendMessage(String message) {
		replyOutput.println(message);
		System.out.println("Reply: *" + message + "* sent.");
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
