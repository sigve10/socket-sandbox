package no.ntnu.sigve.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import no.ntnu.sigve.communication.Message;
import no.ntnu.sigve.communication.UuidMessage;
import no.ntnu.sigve.sockets.ClientSocket;
import no.ntnu.sigve.sockets.TcpClientSocket;
import no.ntnu.sigve.sockets.UdpClientSocket;

/**
 * Represents a client capable of establishing TCP and/or UDP connections to a server.
 * It can send and receive messages using either or both protocols.
 */

public class Client {
	private static final String NOT_CONNECTED_MESSAGE = "Client is not connected";

	private final String address;
	private final LinkedList<Message<?>> incomingMessages;
	private final List<MessageObserver> observers;

	private TcpClientSocket tcpSocket;
	private ClientSocket udpSocket;
	protected UUID sessionId;
	private volatile boolean stopUdpListener = false;
	private volatile Thread udpListenerThread;
	


	/**
     * Constructs a client with the ability to use both TCP and UDP sockets.
     *
     * @param address The server address to connect to.
     * @param tcpPort The server TCP port to connect to.
     * @param udpPort The server UDP port to connect to.
     */
	public Client(String address, int tcpPort, int udpPort) {
		this.address = address;
		this.incomingMessages = new LinkedList<>();
		this.observers = new ArrayList<>();
		this.tcpSocket = new TcpClientSocket(address, tcpPort);
		this.udpSocket = new UdpClientSocket(address, udpPort);

	}

	/**
     * Constructs a client with the ability to use only TCP sockets.
     *
     * @param address The server address to connect to.
     * @param tcpPort The server TCP port to connect to.
     * @param isTcp   Indicator of TCP usage, true to use TCP.
     */
	public Client(String address, int tcpPort, boolean isTcp) {
		this.address = address;
		this.incomingMessages = new LinkedList<>();
		this.observers = new ArrayList<>();
		if (isTcp) {
			this.tcpSocket = new TcpClientSocket(address, tcpPort);
		}
	}

	/**
     * Constructs a client with the ability to use only UDP sockets.
     *
     * @param address The server address to connect to.
     * @param udpPort The server UDP port to connect to.
     */
	public Client(String address, int udpPort) {
		this.address = address;
		this.incomingMessages = new LinkedList<>();
		this.observers = new ArrayList<>();
		this.udpSocket = new UdpClientSocket(address, udpPort);
	}

	/**
     * Initiates the connection process to the server over TCP and/or UDP.
     * A session ID is expected to be received for further communication.
     *
     * @throws IOException            If an I/O error occurs when opening the connection.
     * @throws ClassNotFoundException If the class of the received object cannot be found.
     */
	public void connect() throws IOException, ClassNotFoundException {
		if (tcpSocket != null) {
			tcpSocket.connect();
			listenForTcpMessages(); // Start listening for TCP messages in a separate thread
		}
		if (udpSocket != null) {
			udpSocket.connect();
			this.startUdpListener(); // Start listening for UDP messages
		}
	}
	

	/**
	 * Gets the session ID.
	 *
	 * @return The session ID
	 */
	public UUID getSessionId() {
		if (sessionId == null) {
			throw new IllegalStateException(NOT_CONNECTED_MESSAGE);
		}
		return sessionId;
	}
	/**
     * Sends a message to the server using the appropriate protocol (TCP or UDP).
     *
     * @param message The message to be sent.
     * @throws IOException If an I/O error occurs during sending.
     */
	public void sendOutgoingMessage(Message<?> message) throws IOException {
		System.out.println("Sending message: " + message.getPayload());
		if (message.isUdp() && udpSocket != null) {
			udpSocket.sendMessage(message);
		} else if (tcpSocket != null) {
			tcpSocket.sendMessage(message);
		} else {
			throw new IllegalStateException(NOT_CONNECTED_MESSAGE);
		}
	}
	


	public synchronized void registerIncomingMessage(Message<?> message) {
		System.out.println("Registering incoming message: " + message.getPayload());
		this.incomingMessages.add(message);
		notifyObservers(message);
	}

	public synchronized Message<?> nextIncomingMessage() {
		System.out.println("Checking for incoming messages. Queue size: " + incomingMessages.size());
		if (!this.incomingMessages.isEmpty()) {
			Message<?> nextMessage = this.incomingMessages.remove(0); 
			System.out.println("Returning the next incoming message: " + nextMessage.getPayload());
			return nextMessage;
		}
		System.out.println("No incoming messages available.");
		return null;
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
     * Removes a message observer.
     *
     * @param observer The observer to be removed.
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
	 * Starts a UDP listener in a new thread to receive incoming messages.
	 */
	private void startUdpListener() {
		udpListenerThread = new Thread(() -> {
			try {
				System.out.println("UDP Listener started on port: " + ((UdpClientSocket)udpSocket).getServerPort());
				while (!udpSocket.isClosed()) {
					Message<?> message = udpSocket.receiveMessage();
					if (message != null) {
						registerIncomingMessage(message);
					}
				}
			} catch (IOException e) {
				System.err.println("UDP listener error: " + e.getMessage());
			} catch (Exception e) {
				System.err.println("Unexpected error in UDP listener: " + e.getMessage());
			}
		});
		udpListenerThread.start();
	}


	
	
	
	
	/**
     * Closes the client's TCP and UDP sockets and releases any system resources associated with them.
     *
     * @throws IOException If an I/O error occurs when closing the sockets.
     */
	public void close() throws IOException {
		stopUdpListener = true;
		
		if (udpSocket != null) {
			while (udpListenerThread.isAlive()) {
				try {
					udpListenerThread.join();
				} catch (InterruptedException e) {
					// Ignore interrupted exceptions
				}
			}
		
			udpSocket.close();
		}
	}

	public TcpClientSocket getTcpSocket() {
		return tcpSocket;
	}

	private void listenForTcpMessages() {
		new Thread(() -> {
			try {
				while (!tcpSocket.isClosed()) {
					Message<?> message = tcpSocket.receiveMessage();
					if (message != null) {
						registerIncomingMessage(message);
					}
				}
			} catch (IOException | ClassNotFoundException e) {
				// Handle exceptions as appropriate
			}
		}).start();
	}
}
