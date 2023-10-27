package com.example.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientConnection extends Thread {
	private Socket clientSocket;
	private BufferedReader input;

	public ClientConnection(Socket clientSocket) throws IOException {
		this.clientSocket = clientSocket;
		
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
			message = rawMessage;
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
