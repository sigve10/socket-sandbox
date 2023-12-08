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
import java.util.function.Predicate;

import no.ntnu.sigve.communication.Message;
import no.ntnu.sigve.communication.UuidMessage;

public class Server {
    private final int tcpPort;
    private final int udpPort;
    private ServerSocket tcpServer;
    private DatagramSocket udpServer;
    private final Map<UUID, ServerConnection> tcpConnections;
    private final Map<String, UUID> udpClientSessions; // Maps IP:Port to a session ID
    private final Protocol protocol;
    private volatile boolean running;


    public Server(int tcpPort, int udpPort, Protocol protocol) throws IOException {
        this.tcpPort = tcpPort;
        this.udpPort = udpPort;
        this.tcpServer = new ServerSocket(tcpPort);
        this.udpServer = new DatagramSocket(udpPort);
        this.tcpConnections = new HashMap<>();
        this.udpClientSessions = new HashMap<>();
        this.protocol = protocol;
    }

    public void start() {
        this.running = true;
        new Thread(this::startTcpListener).start();
        new Thread(this::startUdpListener).start();
    }

    private void startTcpListener() {
        while (this.running) {
            try {
                Socket clientSocket = tcpServer.accept();
                handleTcpConnection(clientSocket);
            } catch (IOException e) {
                if (this.running) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void startUdpListener() {
        byte[] buffer = new byte[65535];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        while (this.running) {
            try {
                udpServer.receive(packet);
                handleUdpPacket(packet);
            } catch (IOException e) {
                if (this.running) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleTcpConnection(Socket clientSocket) {
        try {
            UUID sessionId = UUID.randomUUID();
            ServerConnection connection = new ServerConnection(this, clientSocket, sessionId);
            connection.sendMessage(new UuidMessage(sessionId));

            synchronized (this) {
                this.tcpConnections.put(sessionId, connection);
            }

            connection.start();
            this.protocol.onClientConnect(this, sessionId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


	private void handleUdpPacket(DatagramPacket packet) {
		// Log received packet information
		System.out.println("Received UDP packet from " + packet.getAddress().getHostAddress() + ":" + packet.getPort());
	
		// Deserialize the message
		Message<?> message = deserialize(packet.getData(), packet.getLength());
	
		// Use the session ID from the message to identify the client
		UUID sessionId = message.getSessionId();
		if (sessionId == null) {
			// Handle error or create a new session ID
			System.err.println("Received UDP message with no session ID.");
			return;
		}
	
		// Register the UDP client's address with the session ID with synchronization
		String clientKey = packet.getAddress().getHostAddress() + ":" + packet.getPort();
		synchronized (udpClientSessions) {
			if (!udpClientSessions.containsKey(clientKey)) {
				udpClientSessions.put(clientKey, sessionId);
			}
		}
	
		// Log received UDP message payload
		System.out.println("Received UDP message: " + message.getPayload());
	
		this.protocol.receiveMessage(this, message);
	}
	

	public void acceptIncomingConnection(Socket clientSocket) throws IOException {
        UUID sessionId = UUID.randomUUID(); // Generate a unique session ID
        ServerConnection connection = new ServerConnection(this, clientSocket, sessionId);
        
        connection.sendMessage(new UuidMessage(sessionId)); 

        synchronized (this) {
            tcpConnections.put(sessionId, connection);
        }

        connection.start();
        protocol.onClientConnect(this, sessionId);
    }

    public void close() {
        this.running = false;
        if (tcpServer != null && !tcpServer.isClosed()) {
            try {
                tcpServer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (udpServer != null && !udpServer.isClosed()) {
            udpServer.close();
        }
        tcpConnections.values().forEach(ServerConnection::close);
    }
	

	public void broadcast(Message<?> message) {
		// Broadcast to TCP clients
		tcpConnections.values().forEach(conn -> conn.sendMessage(message));
		
		// Broadcast to UDP clients
		udpClientSessions.forEach((clientKey, sessionId) -> {
			InetAddress address = extractAddressFromKey(clientKey);
			int port = extractPortFromKey(clientKey);
			// Serialize the message and send it as a DatagramPacket
			byte[] data = serialize(message);
			DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
			try {
				udpServer.send(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
	
	public void route(Message<?> message) {
		UUID destination = message.getDestination();
	
		// Route to TCP client
		if (tcpConnections.containsKey(destination)) {
			tcpConnections.get(destination).sendMessage(message);
		} else {
			// Route to UDP client
			udpClientSessions.entrySet().stream()
				.filter(entry -> entry.getValue().equals(destination))
				.findFirst()
				.ifPresent(entry -> {
					InetAddress address = extractAddressFromKey(entry.getKey());
					int port = extractPortFromKey(entry.getKey());
					byte[] data = serialize(message);
					DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
					try {
						udpServer.send(packet);
					} catch (IOException e) {
						System.err.println("Error sending UDP message: " + e.getMessage());
					}
				});
		}
	}
	
	private InetAddress extractAddressFromKey(String key) {
		// Extract the InetAddress object from the clientKey
		String[] parts = key.split(":");
		try {
			return InetAddress.getByName(parts[0]);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private int extractPortFromKey(String key) {
		// Extract the port number from the clientKey
		String[] parts = key.split(":");
		return Integer.parseInt(parts[1]);
	}
	

	private Message<?> deserialize(byte[] data, int length) {
    try (ByteArrayInputStream bis = new ByteArrayInputStream(data, 0, length);
         ObjectInputStream in = new ObjectInputStream(bis)) {
        return (Message<?>) in.readObject();
    } catch (IOException | ClassNotFoundException e) {
        e.printStackTrace();
        return null;
    }
}

	private byte[] serialize(Message<?> message) {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos)) {
			out.writeObject(message);
			return bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public synchronized void removeExistingConnection(UUID clientUuid) {

        tcpConnections.remove(clientUuid);
    }

	/**
	 * Notifies the protocol of a new message.
	 *
	 * @param message the message to arrive.
	 */
	public void registerIncomingMessage(Message<?> message) {
		this.protocol.receiveMessage(this, message);
	}
}
