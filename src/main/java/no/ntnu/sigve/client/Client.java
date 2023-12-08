package no.ntnu.sigve.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import no.ntnu.sigve.communication.Message;
import no.ntnu.sigve.communication.UuidMessage;
import no.ntnu.sigve.sockets.ClientSocket;
import no.ntnu.sigve.sockets.ClientSocketFactory;
import no.ntnu.sigve.sockets.TcpClientSocket;
import no.ntnu.sigve.sockets.UdpClientSocket;

/**
 * Represents a client capable of establishing TCP and/or UDP connections to a server.
 * It can send and receive messages using either or both protocols.
 */

 public class Client {
    private static final String NOT_CONNECTED_MESSAGE = "Client is not connected";

    private final String address;
    private final int tcpPort;
    private final int udpPort;
    private final ConcurrentLinkedQueue<Message<?>> incomingMessages;
    private final CopyOnWriteArrayList<MessageObserver> observers;
    private final ClientSocketFactory socketFactory;

    private ClientSocket tcpSocket;
    private ClientSocket udpSocket;
    protected UUID sessionId;
    private Thread tcpListenerThread;
    private Thread udpListenerThread;

    public Client(String address, int tcpPort, int udpPort, ClientSocketFactory socketFactory) {
        this.address = address;
        this.tcpPort = tcpPort;
        this.udpPort = udpPort;
        this.incomingMessages = new ConcurrentLinkedQueue<>();
        this.observers = new CopyOnWriteArrayList<>();
        this.socketFactory = socketFactory;
    }

	public void connect() throws IOException {
        // Connect TCP
        if (tcpPort > 0) {
            tcpSocket = socketFactory.createSocket("TCP", address, tcpPort);
            tcpSocket.connect();
            receiveSessionIdFromTcp();  // Receive session ID from the server
            startListener(tcpSocket, "TCP"); // Listen for TCP messages
        }
        // Connect UDP
        if (udpPort > 0) {
            udpSocket = socketFactory.createSocket("UDP", address, udpPort);
            udpSocket.connect();
            startListener(udpSocket, "UDP"); // Listen for UDP messages
        }
    }

	private void receiveSessionIdFromTcp() throws IOException {
		try {
			Message<?> message = tcpSocket.receiveMessage();
			if (message instanceof UuidMessage) {
				UuidMessage uuidMessage = (UuidMessage) message;
				this.sessionId = uuidMessage.getPayload(); // Retrieves the UUID payload
				System.out.println("Received session ID: " + this.sessionId);
			} else {
				throw new IOException("First message received is not a UUID message.");
			}
		} catch (ClassNotFoundException e) {
			throw new IOException("Error receiving session ID", e);
		}
	}
	
	
	

    private void startListener(ClientSocket socket, String protocol) {
        Thread listenerThread = new Thread(() -> {
            while (!socket.isClosed()) {
                try {
                    Message<?> message = socket.receiveMessage();
                    if (message != null) {
                        registerIncomingMessage(message);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println(protocol + " listener error: " + e.getMessage());
                }
            }
        });
        listenerThread.start();
        if (protocol.equals("TCP")) {
            tcpListenerThread = listenerThread;
        } else if (protocol.equals("UDP")) {
            udpListenerThread = listenerThread;
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
		Message<?> nextMessage = this.incomingMessages.poll();  // Use poll() instead of remove(0)
		if (nextMessage != null) {
			System.out.println("Returning the next incoming message: " + nextMessage.getPayload());
			return nextMessage;
		} else {
			System.out.println("No incoming messages available.");
			return null;
		}
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


	public void close() throws IOException {
		// Close both TCP and UDP sockets
		if (tcpSocket != null) {
			tcpSocket.close();
		}
		if (udpSocket != null) {
			udpSocket.close();
		}
		// Attempt to join both listener threads
		try {
			if (tcpListenerThread != null && tcpListenerThread.isAlive()) {
				tcpListenerThread.join();
			}
			if (udpListenerThread != null && udpListenerThread.isAlive()) {
				udpListenerThread.join();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		try {
			Thread.sleep(1000); // Adjust the delay as needed
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
}
