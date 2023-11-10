package com.example.server;

import com.example.server.commands.CommandSet;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A server running on the target machine. Can listen to incoming client connections and handle
 * clients independently. Once {@link Server#start() start()} is called, the server will be
 * occupied with a listening loop. The server can be shut down by calling {@link Server#shutDown()
 * shutDown()}
 *
 * @author Sigve Bjørkedal
 */
public class Server {
	private static boolean shutdownInitiated = false;

	private final int port;
	private CommandSet commandSet;
	private ServerSocket server;

	/**
	 * Creates a new server on the given port.
	 *
	 * @param port the listening port of the server
	 */
	public Server(int port) {
		this.port = port;
	}

	/**
	 * Causes the server to start listening for new connections and handle incoming messages.
	 *
	 * @throws IOException if the creation of a server fails
	 */
	public void start() throws IOException {
	
		this.server = new ServerSocket(port);
		System.out.println("Server started on port " + this.port);

		do {
			Socket client = this.server.accept();
			System.out.println("Connection from " + client.getInetAddress());
			ClientConnection connection = new ClientConnection(commandSet, client);
			connection.start();
		} while (!Server.shutdownInitiated);

		System.out.println("Servers have shut down. Terminating.");
	}

	/**
	 * Sets a list of commands that the server should be able to interpret and react to.
	 *
	 * @param commandSet a commandSet containing the commands the server should be able to handle
	 */
	public void setCommandSet(CommandSet commandSet) {
		this.commandSet = commandSet;
	}
	
	/**
	 * Attempts to shut down the server and close all client connections.
	 */
	public static void shutDown() {
		System.out.println("Shutting down servers");
		Server.shutdownInitiated = true;
	}
}
