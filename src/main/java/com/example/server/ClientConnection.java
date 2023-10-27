package com.example.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.example.server.commands.CommandSet;

public class ClientConnection extends Thread {
	private Socket clientSocket;
	private BufferedReader input;
	private CommandSet commandSet;
	private PrintWriter replyOutput;

	public ClientConnection(CommandSet commandSet, Socket clientSocket) throws IOException {
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
				reply(response);
			}
		} while (response != null && !response.equalsIgnoreCase("Disconnect"));

		this.close();
	}

	private String readClientRequest() {
		String message = null;
		
		try {
			String rawMessage = input.readLine();
			System.out.println(" >>> " + rawMessage);
			reply(rawMessage);

			message = this.commandSet.tryToExecuteCommand(rawMessage);
		} catch (IOException e) {
			System.err.println("Could not handle request. " + e.getMessage());
		}
		
		return message;
	}

	/**
	 * Send a reply back to the client
	 * 
	 * @param message The message that will be sent to the client
	 */
	public void reply(String message) {
		replyOutput.println(message);
		System.out.println("Reply: *" + message + "* sent.");
	}

	public void close() {
		try {
			this.clientSocket.close();
			input.close();
		} catch (IOException e) {
			System.err.println("Could not close socket. " + e.getMessage());
		}
	}
}
