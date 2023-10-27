package com.example.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Here is the javadoc for the client :)
 */
public class Client {
	private Socket socket;
	private BufferedReader input;
	private PrintWriter output;
	private BufferedReader replyInput;

	/**
	 * Constructor for the Client
	 * 
	 * @param address 
	 * @param port Port the client will connect to
	 * @throws IOException if shit hits the fan
	 */
	public Client(String address, int port) throws IOException {
		try {
			this.socket = new Socket(address, port);
			System.out.println("Client: Connected to " + address + ":" + port);
			this.input = new BufferedReader(new InputStreamReader(System.in));
			this.output = new PrintWriter(socket.getOutputStream(), true);
			this.replyInput = new BufferedReader(
				new InputStreamReader(
					socket.getInputStream()
				)
			);
		} catch(UnknownHostException e) {
			System.err.println("Could not connect to " + address + ":" + port);
		}
		startReadingInput();
	}

	/**
	 * Reads input from command line
	 */
	private void startReadingInput() {
		String message = null;
		do {
			try {
				message = this.input.readLine();
				sendMessage(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} while (message == null || !message.equalsIgnoreCase("disconnect"));
		close();
	}

	/**
	 * sends a message to the server
	 */
	private void sendMessage(String messsage){
		output.println(messsage);
		listenForReply();
	}

	/**
	 * Closes the connection.
	 */
	private void close() {
		try {
			this.input.close();
			this.output.close();
			this.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Listens for a reply from the server
	 */
	private void listenForReply() {
		String message = null;
		
		do{
			try {
				message = replyInput.readLine();
				System.out.println("Recived reply " + message);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} while(message == null);
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
