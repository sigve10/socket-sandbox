package no.ntnu.sigve.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.function.Predicate;

import no.ntnu.sigve.communication.AckMessage;
import no.ntnu.sigve.communication.Message;
import no.ntnu.sigve.communication.UuidMessage;

public class Server {
	private final int port;
	private final ServerSocket genericServer;
	private final Map<UUID, InetAddress> uuidToAddressMap;
	private final Map<UUID, ServerConnection> clientConnections;
	private final Protocol protocol;

	private DatagramSocket udpSocket;
	private Thread udpListenerThread;
	private CountDownLatch readyLatch = new CountDownLatch(1);

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
		this.uuidToAddressMap = new HashMap<>();
		this.clientConnections = new HashMap<>();
		this.port = port;
	}

	/**
	 * Causes the server to start listening for new connections and handle incoming messages.
	 */
	public void start() {
		System.out.println("Server started on port " + this.port);
		new ServerIncomingConnectionListener(this, genericServer).start();
		startUdpListener();
		readyLatch.countDown();
	}

	/**
	 * Closes the server, and terminates all connected clients.
	 */
	public void close() {
		// Close UDP Socket
		if (udpSocket != null) {
			udpSocket.close(); // DatagramSocket#close() does not throw IOException
		}
	
		// Interrupt UDP Listener Thread
		if (udpListenerThread != null && udpListenerThread.isAlive()) {
			udpListenerThread.interrupt();
		}
	
		// Close each Server Connection
		clientConnections.values().forEach(connection -> {
			try {
				connection.close();
			} catch (Exception e) {
				System.err.println("Error closing ServerConnection: " + e.getMessage());
			}
		});
	
		// Close Server Socket
		if (genericServer != null) {
			try {
				genericServer.close();
			} catch (IOException e) {
				System.err.println("Error closing ServerSocket: " + e.getMessage());
			}
		}
	}
	
	

	/**
	 * Attempts to accept an incoming connection and create a handler for it.
	 *
     * @param incomingConnection the client requesting a connection
     * @throws IOException if the connection is refused
     */
	public void acceptIncomingConnection(Socket incomingConnection) {
		try {
			UUID sessionId = UUID.randomUUID();
			ServerConnection connection = new ServerConnection(this, incomingConnection, sessionId);
			connection.sendMessage(new UuidMessage(sessionId));
			System.out.println("Accepted incoming connection from client with session ID: " + sessionId);
	
			synchronized (this) {
				this.uuidToAddressMap.put(sessionId, incomingConnection.getInetAddress());
				this.clientConnections.put(sessionId, connection);
			}
	
			connection.start();
			this.protocol.onClientConnect(this, sessionId);
		} catch (IOException e) {
			System.err.println("Error handling incoming connection: " + e.getMessage());
		}
	}
	

	/**
	 * Removes an existing connection from the map of connections. Notifies the protocol.
	 *
	 * @param sessionId the UUID of the disconnecting client.
	 */
	public void removeExistingConnection(UUID sessionId) {
		this.protocol.onClientDisconnect(this, sessionId);
	
		synchronized (this) {
			ServerConnection connection = this.clientConnections.remove(sessionId);
			if (connection != null) {
				connection.close();
			}
			this.uuidToAddressMap.remove(sessionId);
		}
	}
	
	

	/**
	 * Broadcasts a given message to all currently connected clients. This method
	 * iterates through each client connection and sends the specified message.
	 *
	 * @param message The message to be broadcasted to all clients.
		*/
	public void broadcast(Message<?> message) {
		for (ServerConnection connection : clientConnections.values()) {
			connection.sendMessage(message);

		}
	}

	/**
	 * Broadcasts a given message to all currently connected clients according to a predicate.
	 *
	 * @param message the message to be broadcast
	 * @param predicate a predicate to filter the session IDs
	 */
	public void broadcastFiltered(Message<?> message, Predicate<UUID> predicate) {
		clientConnections.keySet().stream()
			.filter(predicate)
			.forEach(key -> clientConnections.get(key).sendMessage(message));
	}

	/**
	 * Routes a message to one specific target address. If the target does not exist, the message
	 * is discarded.
	 *
	 * @param message the message to be sent
	 */

	 public void route(Message<?> message) {
		System.out.println("Routing message to: " + message.getDestination());
		synchronized (this) {
			if (clientConnections.containsKey(message.getDestination())) {
				System.out.println("Found client connection for session ID: " + message.getDestination());
				clientConnections.get(message.getDestination()).sendMessage(message);
				System.out.println("Message routed to client with session ID: " + message.getDestination());
			} else {
				System.out.println("Target client not found for session ID: " + message.getDestination() + ", discarding message");
			}
		}
	}
	
	
	/**
	 * Notifies the protocol of a new message.
	 *
	 * @param message the message to arrive.
	 */
	public void registerIncomingMessage(Message<?> message) {
		this.protocol.receiveMessage(this, message);
		System.out.println("Received message from client with session ID: " + message.getSessionId());

	}

	/**
     * Sends a message via UDP to a specified client.
     *
     * @param message The message to send.
     * @param clientAddress The address of the client.
     * @param clientPort The port of the client.
     * @throws IOException If an error occurs during sending.
		*/
	public void sendUdpMessage(Message<?> message, InetAddress clientAddress, int clientPort) throws IOException {
		byte[] messageData = serializeMessage(message);
		DatagramPacket packet = new DatagramPacket(messageData, messageData.length, clientAddress, clientPort);
		udpSocket.send(packet);
	}

	/**
     * Starts a UDP listener thread that listens for incoming UDP packets.
     * When a packet is received, it is converted into a message and processed.
     */
	private void startUdpListener() {
		udpListenerThread = new Thread(() -> {
			try {
				udpSocket = new DatagramSocket(port);
				byte[] buffer = new byte[65535];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
	
				while (!udpSocket.isClosed()) {
					udpSocket.receive(packet);
					handleUdpPacket(packet);
				}
			} catch (IOException e) {
				System.err.println("UDP listener error: " + e.getMessage());
				// Consider restarting the UDP listener or handling the error appropriately
			} catch (Exception e) {
				System.err.println("Unexpected error in UDP listener: " + e.getMessage());
				// Additional error handling
			}
		});
		udpListenerThread.start();
	}
	

	/**
	 * Handles a received UDP packet by deserializing it into a message and processing it.
	 * If the message is valid, an acknowledgment is sent back to the client.
	 *
	 * @param packet The received UDP packet to handle.
	 */
	private void handleUdpPacket(DatagramPacket packet) {
		Message<?> message = null;
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData(), packet.getOffset(), packet.getLength());
			ObjectInputStream ois = new ObjectInputStream(bais);
			message = (Message<?>) ois.readObject();
			System.out.println("UDP packet received from: " + packet.getAddress());
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Problem with handling UDP packet: " + e.getMessage());
			return;
		}
		sendAck(packet.getAddress(), packet.getPort(), message);
	}

	/**
	 * Sends an acknowledgment (ACK) message back to the client to confirm receipt of a message.
	 *
	 * @param clientAddress The IP address of the client to send the ACK to.
	 * @param clientPort The port number of the client to send the ACK to.
	 * @param receivedMessage The message that was received and is being acknowledged.
	 * @throws IOException If there is an error sending the ACK.
	 */
	private void sendAck(InetAddress clientAddress, int clientPort, Message<?> receivedMessage) {
		try {
			AckMessage ack = new AckMessage(receivedMessage.getSessionId());
			byte[] ackData = serializeMessage(ack);
			DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length, clientAddress, clientPort);
			udpSocket.send(ackPacket);
			System.out.println("ACK sent for message with ID: " + receivedMessage.getSessionId());
		} catch (IOException e) {
			System.out.println("Error sending ACK: " + e.getMessage());
		}
	}

	/**
	 * Serializes a message into a byte array for transmission over a socket.
	 *
	 * @param message The message to serialize.
	 * @return The serialized message as a byte array.
	 * @throws IOException If there is an error during serialization.
	 */
	private byte[] serializeMessage(Message<?> message) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(message);
		oos.flush();
		return baos.toByteArray();
	}


	public void awaitReady() throws InterruptedException {
		readyLatch.await();
	}

}
