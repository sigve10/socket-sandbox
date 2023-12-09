package no.ntnu.sigve.sockets;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import no.ntnu.sigve.communication.Message;

public class TcpClientSocket implements ClientSocket {

		private final String serverAddress;
		private final int serverPort;
		private Socket socket;
		private ObjectOutputStream outputStream;
		private ObjectInputStream inputStream;

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
	}

	@Override
	public void sendMessage(Message<?> message) throws IOException {
		if (socket == null || socket.isClosed()) {
			throw new IOException("Socket is not connected");
		}
		try {
			System.out.println("Sending message via TCP: " + message);
			outputStream.writeObject(message);
		} catch (IOException e) {
			System.out.println("Error sending TCP message: " + e.getMessage());
			throw e;
		}
	}

	@Override
	public Message<?> receiveMessage()
		throws IOException, ClassNotFoundException {
		if (socket == null || socket.isClosed()) {
			throw new IOException("Socket is not connected");
		}
		try {
			System.out.println("Waiting to receive TCP message...");
			Message<?> message = (Message<?>) inputStream.readObject();
			System.out.println("Received TCP message: " + message);
			return message;
		} catch (IOException | ClassNotFoundException e) {
			System.err.println("Error receiving TCP message: " + e.getMessage());
			throw e;
		}
	}

	@Override
	public boolean isClosed() {
		return socket == null || socket.isClosed();
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
}
