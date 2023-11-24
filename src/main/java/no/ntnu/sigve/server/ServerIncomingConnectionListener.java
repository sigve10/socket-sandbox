package no.ntnu.sigve.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A thread for listening and attempting to accept incoming client connections.
 */
public class ServerIncomingConnectionListener extends Thread {
	private final Server server;
	private final ServerSocket serverSocket;

	/**
	 * Creates a new incoming connection listener for a {@link Server}.
	 *
	 * @param server the server for which this listener is listening
	 * @param serverSocket the socket from which to accept connections
	 */
	public ServerIncomingConnectionListener(Server server, ServerSocket serverSocket) {
		this.server = server;
		this.serverSocket = serverSocket;
	}

	@Override
	public void run() {
		while (!serverSocket.isClosed()) {
			try {
				Socket client = this.serverSocket.accept();
				this.attemptToAcceptConnection(client);
			} catch (IOException e) {
				if (!serverSocket.isClosed()) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Attempts to form a connection with the server.
	 *
	 * @param connection the client to connect
	 * @throws IOException if the connection is refused
	 */
	private synchronized void attemptToAcceptConnection(Socket connection) throws IOException {
		this.server.acceptIncomingConnection(connection);
	}
}
