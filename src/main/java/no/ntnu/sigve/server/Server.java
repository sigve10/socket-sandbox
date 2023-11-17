package no.ntnu.sigve.server;

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
	private final int port;
	private ServerSocket genericServer;
	private HashMap<String, ServerConnection> clientConnections;
	private final Protocol protocol;

	/**
	 * Creates a new server on the given port, with the given protocol to interpret messages.
	 *
	 * @param port the port on which the server will listen for incoming connections.
	 * @param protocol the protocol by which the server will interpret messages.
	 * @throws IOException if creating the server fails.
	 */
	public Server(int port, Protocol protocol) throws IOException {
		this.genericServer = new ServerSocket(port);
		this.protocol = protocol;
		this.clientConnections = new HashMap<>();
		this.port = port;
	}

	/**
	 * Causes the server to start listening for new connections and handle incoming messages.
	 */
	public void start() {
		System.out.println("Server started on port " + this.port);
		new ServerIncomingConnectionListener(this, genericServer).start();
	}

	/**
	 * Attempts to accept an incoming connection and create a handler for it.
	 *
	 * @param incomingConnection the client requesting a connection
	 * @throws IOException if the connection is refused
	 */
	public void acceptIncomingConnection(Socket incomingConnection) throws IOException {
		ServerConnection connection = new ServerConnection(this.protocol, incomingConnection);
		this.clientConnections.put(incomingConnection.getInetAddress().toString(), connection);
		connection.start();
		System.out.println("Connection from " + incomingConnection.getInetAddress());
	}

	/**
	 * Broadcasts a single message to all clients currently connected.
	 *
	 * @param message the message to be broadcasted
	 */
	public void broadcast(String message) {
		clientConnections.values().forEach(client -> client.sendMessage(message));
	}

	/**
	 * Routes a message to one specific target address. If the target does not exist, the message
	 * is discarded.
	 *
	 * @param targetAddress the address to which the message should be sent
	 * @param message the message to be sent
	 */
	public void route(String targetAddress, String message) {
		if (clientConnections.containsKey(targetAddress)) {
			clientConnections.get(targetAddress).sendMessage(message);
		} else {
			System.out.println("Target client not found, discarding message");
		}
	}

	public static void main(String args[]) {
		System.out.println("Yay");
	}
}
