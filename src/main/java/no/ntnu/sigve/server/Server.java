package no.ntnu.sigve.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.UUID;

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
	private HashMap<InetAddress, ServerConnection> clientConnections;
	private final Protocol protocol;
	private static Server instance;

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
		instance = this;
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
        UUID sessionId = UUID.randomUUID();
        ServerConnection connection = new ServerConnection(this.protocol, incomingConnection);
        
        PrintWriter out = new PrintWriter(incomingConnection.getOutputStream(), true);
        out.println("SessionID:" + sessionId.toString());

        this.clientConnections.put(incomingConnection.getInetAddress(), connection);
        connection.start();
        System.out.println("Connection from " + incomingConnection.getInetAddress() + ", Session ID: " + sessionId);
    }


	 /**
	 * Broadcasts a given message to all currently connected clients. This method
	 * iterates through each client connection and sends the specified message.
	 *
	 * @param message The message to be broadcasted to all clients.
	 */
	public void broadcast(String message) {
		for (ServerConnection connection : clientConnections.values()) {
			connection.sendMessage(message);
		}
	}

	/**
	 * Routes a message to one specific target address. If the target does not exist, the message
	 * is discarded.
	 *
	 * @param targetAddress the address to which the message should be sent
	 * @param message the message to be sent
	 */
	public void route(InetAddress targetAddress, String message) {
		if (clientConnections.containsKey(targetAddress)) {
			clientConnections.get(targetAddress).sendMessage(message);
		} else {
			System.out.println("Target client not found, discarding message");
		}
	}


	/**
	 * Provides access to the current instance of the Server. 
	 * @return The current instance of the Server.
	 */
	public static Server getInstance() {
		return instance;
	}
	
}
