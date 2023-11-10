package com.example.server;

import com.example.server.commands.CommandSet;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

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
	private HashMap<String, ClientConnection> clientConnections;

	/**
	 * Creates a new server on the given port.
	 *
	 * @param port the listening port of the server
	 * @throws IOException if server creation fails
	 */
	public Server(int port) throws IOException {
		this.server = new ServerSocket(port);
		this.commandSet = null;
		this.clientConnections = new HashMap<>();
		this.port = port;
	}

	/**
	 * Causes the server to start listening for new connections and handle incoming messages.
	 */
	public void start() {
		System.out.println("Server started on port " + this.port);
		new ServerIncomingConnectionListener(this, server).start();
	}

	/**
	 * Attempts to accept an incoming connection and create a handler for it.
	 *
	 * @param incomingConnection the client requesting a connection
	 * @throws IOException if the connection is refused
	 */
	public void acceptIncomingConnection(Socket incomingConnection) throws IOException {
		ClientConnection connection = new ClientConnection(commandSet, incomingConnection);
		this.clientConnections.put(incomingConnection.getInetAddress().toString(), connection);
		connection.start();
		System.out.println("Connection from " + incomingConnection.getInetAddress());
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
