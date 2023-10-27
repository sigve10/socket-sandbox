package com.example.server;

import java.io.IOException;
import java.net.Socket;

import com.example.server.commands.CommandSet;

import java.net.ServerSocket;

public class Server {
	private static boolean shutdownInitiated = false;

	private final int port;
	private CommandSet commandSet;
	private ServerSocket server;

	public Server(int port) throws IOException {
		this.port = port;
		this.server = new ServerSocket(port);
		System.out.println("Server started on port " + port);

		do {
			Socket client = this.server.accept();
			System.out.println("Connection from " + client.getInetAddress());
			ClientConnection connection = new ClientConnection(commandSet, client);
			connection.start();
		} while (!Server.shutdownInitiated);

		System.out.println("Servers have shut down. Terminating.");
	}

	public void setCommandSet(CommandSet commandSet) {
		this.commandSet = commandSet;
	}
	
	public static void shutDown() {
		System.out.println("Shutting down servers");
		Server.shutdownInitiated = true;
	}

	public static void main(String[] args) {
		try {
			Server server = new Server(8080);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
