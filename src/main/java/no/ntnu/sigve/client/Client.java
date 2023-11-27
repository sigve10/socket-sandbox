package no.ntnu.sigve.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import no.ntnu.sigve.communication.AckMessage;
import no.ntnu.sigve.communication.Message;
import no.ntnu.sigve.communication.UuidMessage;

/**
 * A client connection to a server. Capable of continuously reading information from the client and
 * sending messages.
 *
 * @author Sigve Bjørkedal
 * @see Client#sendOutgoingMessage(Message) sendOutgoingMessage
 * @see Client#nextIncomingMessage() nextIncomingMessage
 */
public class Client {
	private static final String NOT_CONNECTED_MESSAGE = "Client is not connected";

	private final String address;
	private final int port;
	private final LinkedList<Message<?>> incomingMessages;
	private final List<MessageObserver> observers;

	private ObjectOutputStream output;
	private Socket socket;
	protected UUID sessionId;

	private DatagramSocket udpSocket;
	private InetAddress udpAddress;
	private int udpPort;
	private Thread udpListenerThread;
	private static final int ACK_TIMEOUT = 2000; 

	/**
	 * Creates a new client connection to a server.
	 *
	 * @param address the address of the server to connect to
	 * @param port    the port of the server to connect to
	 */
	public Client(String address, int port) {
		this.address = address;
		this.port = port;
		this.incomingMessages = new LinkedList<>();
		this.observers = new ArrayList<>();
	}



	public Client(String address, int tcpPort, int udpPort) throws IOException {
		this(address, tcpPort);
		this.udpPort = udpPort;
		this.udpAddress = InetAddress.getByName(address);
		this.udpSocket = new DatagramSocket();
		this.udpSocket = createDatagramSocket();
	}

	/**
	 * Tries to connect to the server.
	 *
	 * @throws IOException If connecting to the server fails
	 */
	public void connect() throws IOException {
		this.socket = new Socket(address, port);

		ObjectInputStream socketResponseStream =
				new ObjectInputStream(this.socket.getInputStream());
		this.output = new ObjectOutputStream(this.socket.getOutputStream());

		UUID uuid = null;
		try {
			UuidMessage uuidMessage = (UuidMessage) socketResponseStream.readObject();
			if (uuidMessage != null) {
				uuid = uuidMessage.getPayload();
			}
		} catch (ClassNotFoundException | ClassCastException e) {
			//Do nothing
		}
		if (uuid != null) {
			this.sessionId = uuid;
			System.out.println("Received session ID: " + this.sessionId);
		} else {
			throw new IllegalStateException("Session ID was not received properly.");
		}

		new ClientListener(this, socketResponseStream).start();
	}

	/**
	 * Gets the session ID.
	 *
	 * @return The session ID
	 */
	public UUID getSessionId() {
		if (socket == null) {
			throw new IllegalStateException(NOT_CONNECTED_MESSAGE);
		}
		return sessionId;
	}

	/**
	 * Sends a message to the server.
	 *
	 * @param message to send to the server.
	 */
	public void sendOutgoingMessage(Message<?> message) {
		if (socket == null) {
			throw new IllegalStateException(NOT_CONNECTED_MESSAGE);
		}
		try {
			this.output.writeObject(message);
		} catch (IOException ioe) {
			System.err.println("Could not send outgoing message. Here's the stacktrace:");
			ioe.printStackTrace();
		}
	}

	/**
	 * Attempts to retrieve the earliest received message from the server.
	 *
	 * @return the earliest received message, or null if it does not exist.
	 */
	public Message<?> nextIncomingMessage() {
		Message<?> retval = null;

		if (this.incomingMessages.peek() != null) {
			retval = this.incomingMessages.removeFirst();
		}

		return retval;
	}

	/**
	 * Registers a new message to the client. Can be read through {@link Client#nextIncomingMessage
	 * nextIncomingMessage}.
	 *
	 * @param message the message to register.
	 */
	public void registerIncomingMessage(Message<?> message) {
		this.incomingMessages.add(message);
		notifyObservers(message);
	}

	/**
	 * Adds an observer to the client. The observer will be notified of new messages.
	 *
	 * @param observer The observer to add.
	 */
	public void addObserver(MessageObserver observer) {
		this.observers.add(observer);
	}

	/**
	 * Removes an observer from the client.
	 * The observer will no longer receive message notifications.
	 *
	 * @param observer The observer to remove.
	 */
	public void removeObserver(MessageObserver observer) {
		this.observers.remove(observer);
	}

	/**
	 * Notifies all registered observers with the given message. This method is called
	 * when a new message is received and needs to be communicated to all observers.
	 *
	 * @param message The message to be sent to the observers.
	 */
	private void notifyObservers(Message<?> message) {
		for (MessageObserver observer : this.observers) {
			observer.update(message);
		}
	}

	/**
	 * Starts a UDP listener that continuously listens for UDP messages.
	 * When a message is received, it is registered and observers are notified.
	 */
	public void startUdpListener() {
		udpListenerThread = new Thread(() -> {
			try {
				while (!udpSocket.isClosed()) {
					Message<?> message = receiveUdpMessage();
					System.out.println("UDP message received");
					registerIncomingMessage(message);
				}
			} catch (IOException | ClassNotFoundException e) {
				System.out.println("UDP listener error: " + e.getMessage());
			}
		});
		udpListenerThread.start();
	}


	/**
	 * Sends a message over UDP to the server. The message is serialized to a byte array and then
	 * sent in a UDP packet to the specified server address and port.
	 *
	 * @param message The message to be sent to the server.
	 * @throws IOException if an I/O error occurs while creating the output stream or if the socket
	 *                     is not connected to a remote address and port when sending the packet.
	 */

	public void sendUdpMessage(Message<?> message) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(message);
		byte[] buffer = baos.toByteArray();

		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, this.udpAddress, this.udpPort);
		this.udpSocket.send(packet);
	}

	/**
	 * Sends a UDP message and waits for an acknowledgement (ACK).
	 * If an ACK is not received within a specified timeout, the message is resent.
	 *
	 * @param message The message to send over UDP.
	 * @throws IOException If there is an error sending the message.
	 */
	public void sendUdpMessageWithAck(Message<?> message) throws IOException {
		sendUdpMessage(message);
		try {
			udpSocket.setSoTimeout(ACK_TIMEOUT);
			while (true) {
				try {
					Message<?> ack = receiveUdpMessage();
					if (ack instanceof AckMessage && ((AckMessage) ack).isAcknowledgementFor(message)) {
						System.out.println("ACK received for message with ID: " + message.getSessionId());
						break;
					}
				} catch (SocketTimeoutException e) {
					System.out.println("ACK not received, resending message with ID: " + message.getSessionId());
					sendUdpMessage(message);
				} catch (ClassNotFoundException e) {
					System.out.println("Class not found while receiving message: " + e.getMessage());
				}
			}
		} finally {
			udpSocket.setSoTimeout(0);
		}
	}



	/**
	 * Receives a UDP message from the socket.
	 *
	 * @return The message received from the UDP socket.
	 * @throws IOException If there is an error receiving the message.
	 * @throws ClassNotFoundException If the serialized object class is not found.
	 */
	public Message<?> receiveUdpMessage() throws IOException, ClassNotFoundException {
		byte[] buffer = new byte[65535];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		this.udpSocket.receive(packet);

		ByteArrayInputStream bais = new ByteArrayInputStream(buffer, packet.getOffset(), packet.getLength());
		ObjectInputStream ois = new ObjectInputStream(bais);
		return (Message<?>) ois.readObject();
	}
		
	/**
	 * Closes the client connection, including both TCP and UDP sockets, and stops the listener thread.
	 *
	 * @throws IOException If an I/O error occurs when closing the connection.
	 */
	public void close() throws IOException {
		if (socket != null && !socket.isClosed()) {
			socket.close();
		}
		if (output != null) {
			output.close();
		}
		if (udpSocket != null && !udpSocket.isClosed()) {
			udpSocket.close();
		}
		if (udpListenerThread != null) {
			udpListenerThread.interrupt();
		}
	}

	protected DatagramSocket createDatagramSocket() throws IOException {
		return new DatagramSocket();
	}
}
