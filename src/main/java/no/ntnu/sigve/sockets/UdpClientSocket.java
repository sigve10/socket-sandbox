package no.ntnu.sigve.sockets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import no.ntnu.sigve.communication.Message;

/**
 * .
 */
public class UdpClientSocket implements ClientSocket {

	private final String serverAddress;
	private final int serverPort;
	private DatagramSocket socket;
	private InetAddress inetAddress;
	private DatagramPacket packet;

	/**
	 * .
	 *
	 * @param serverAddress .
	 * @param serverPort .
	 */
	public UdpClientSocket(String serverAddress, int serverPort) {
		if (serverPort <= 0 || serverPort > 65535) {
			throw new IllegalArgumentException("Invalid port number");
		}
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;

		byte[] buffer = new byte[65535];
		this.packet = new DatagramPacket(buffer, buffer.length);
	}

	@Override
	public void sendMessage(Message<?> message) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(message);
		byte[] buffer = baos.toByteArray();

		DatagramPacket sendPacket = new DatagramPacket(
			buffer, buffer.length, inetAddress, serverPort);
		System.out.println("Sending message via UDP: " + message);
		socket.send(sendPacket);
	}

	@Override
	public Message<?> receiveMessage() throws IOException, ClassNotFoundException {
		socket.receive(this.packet);

		ByteArrayInputStream bais = new ByteArrayInputStream(
			packet.getData(),
			packet.getOffset(),
			packet.getLength()
		);

		ObjectInputStream ois = new ObjectInputStream(bais);

		return (Message<?>) ois.readObject();
	}

	@Override
	public void connect() throws IOException {
		this.socket = new DatagramSocket(serverPort);
	}

	@Override
	public void close() throws IOException {
		if (socket != null && !socket.isClosed()) {
			socket.close();
		}
	}
}
