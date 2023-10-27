package com.example.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import com.example.server.commands.CommandSet;

public class ClientConnection extends Thread {
	private Socket clientSocket;
	private BufferedReader input;
	private CommandSet commandSet;

	public ClientConnection(CommandSet commandSet, Socket clientSocket) throws IOException {
		this.clientSocket = clientSocket;
		this.commandSet = commandSet;
		
		input = new BufferedReader(
			new InputStreamReader(
				clientSocket.getInputStream()
			)
		);
	}

	@Override
	public void run() {
		String response;

		do {
			response = readClientRequest();
			if (response != null) {
				System.out.println("Server: Received message \"" + response + "\"");
			}
		} while (response != null && !response.equalsIgnoreCase("Disconnect"));

		this.close();
	}

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

	public void close() {
		try {
			this.clientSocket.close();
			input.close();
		} catch (IOException e) {
			System.err.println("Could not close socket. " + e.getMessage());
		}
	}
}
