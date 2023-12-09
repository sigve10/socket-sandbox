package no.ntnu.sigve.sockets;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;
import no.ntnu.sigve.communication.Message;
import no.ntnu.sigve.communication.UuidMessage;

/**
 * A ClientSocket for a TCP connection.
 */
public class TcpClientSocket implements ClientSocket {
	private final String serverAddress;
	private final int serverPort;
	private Socket socket;
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;

	private UUID sessionId;

	/**
	 * Creates a new TCPClientSocket.
	 *
	 * @param serverAddress the address for the socket to connect to
	 * @param serverPort the port for the socket to connect to
	 */
	public TcpClientSocket(String serverAddress, int serverPort) {
		if (serverPort <= 0 || serverPort > 65535) {
			throw new IllegalArgumentException("Invalid port number");
		}
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
	}

	@Override
	public void connect() throws IOException {
		this.socket = new Socket(serverAddress, serverPort);
		this.outputStream = new ObjectOutputStream(socket.getOutputStream());
		this.inputStream = new ObjectInputStream(socket.getInputStream());

		try {
			UuidMessage message = (UuidMessage) receiveMessage();
			this.sessionId = message.getPayload();
		} catch (ClassNotFoundException e) {
			throw new IOException("Could not connect as no session ID was received");
		}
	}

	@Override
	public void sendMessage(Message<?> message) throws IOException {
		if (socket == null || socket.isClosed()) {
			throw new IOException("Socket is not connected");
		}

		outputStream.writeObject(message);
	}

	@Override
	public Message<?> receiveMessage()
		throws IOException, ClassNotFoundException {
		if (socket == null || socket.isClosed()) {
			throw new IOException("Socket is not connected");
		}

		return (Message<?>) inputStream.readObject();
	}

	@Override
	public void close() throws IOException {
		if (socket != null && !socket.isClosed()) {
			socket.close();
		}
		if (outputStream != null) {
			outputStream.close();
		}
		if (inputStream != null) {
			inputStream.close();
		}
	}

	/**
	 * Gets the TCP socket's session ID.
	 *
	 * @return the TCP socket's session ID
	 */
	public UUID getSessionId() {
		return this.sessionId;
	}
}
