package com.example.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
	private Socket socket;
	private BufferedReader input;
	private PrintWriter output;

	public Client(String address, int port) throws IOException {
		try {
			this.socket = new Socket(address, port);
			System.out.println("Client: Connected to " + address + ":" + port);
			this.input = new BufferedReader(new InputStreamReader(System.in));
			this.output = new PrintWriter(socket.getOutputStream(), true);
		} catch(UnknownHostException e) {
			System.err.println("Could not connect to " + address + ":" + port);
		}

		startReadingInput();
	}

	private void startReadingInput() {
		String message = null;
		do {
			try {
				message = this.input.readLine();
				output.println(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} while (message == null || !message.equalsIgnoreCase("disconnect"));

		close();
	}

	private void close() {
		try {
			this.input.close();
			this.output.close();
			this.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public static void main(String args[]) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		int maxAttempts = 3;
	
		for (int i = 0; i < maxAttempts; i++) {
			try {
				System.out.print("Enter server address (default: localhost): ");
				String address = reader.readLine().trim();
				if (address.isEmpty()) {
					address = "localhost";
				}
	
				System.out.print("Enter server port (default: 8080): ");
				String portStr = reader.readLine().trim();
				int port = portStr.isEmpty() ? 8080 : Integer.parseInt(portStr);
	
				Client client = new Client(address, port);
				break; 
			} catch (IOException e) {
				if (i < maxAttempts - 1) {
					System.out.println("Failed to connect. Retrying (" + (i + 2) + "/" + maxAttempts + ")...");
				} else {
					System.out.println("Failed to connect after " + maxAttempts + " attempts.");
					e.printStackTrace();
				}
			}
		}
	}
	
}
