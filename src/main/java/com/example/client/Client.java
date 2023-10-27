package com.example.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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
		try {
			Client client = new Client("localhost", 8080);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
