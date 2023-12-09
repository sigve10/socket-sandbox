package no.ntnu.sigve.client;

import java.io.IOException;
import java.util.UUID;
import no.ntnu.sigve.communication.Message;
import no.ntnu.sigve.communication.Protocol;
import no.ntnu.sigve.communication.ProtocolUser;
import no.ntnu.sigve.sockets.ClientSocket;
import no.ntnu.sigve.sockets.ClientSocketFactory;
import no.ntnu.sigve.sockets.TcpClientSocket;
import no.ntnu.sigve.sockets.TransportProtocol;
import no.ntnu.sigve.sockets.UdpClientSocket;

/**
 * A client connection to a server. Capable of continuously reading information from the client and
 * sending messages.
 *
 * @author Sigve Bjørkedal
 * @see Client#sendOutgoingMessage(Message) sendOutgoingMessage
 */
public class Client implements ProtocolUser {
	private static final String NOT_CONNECTED_MESSAGE = "Client is not connected";

	private final String address;
	private final int tcpPort;
	private final int udpPort;
	private final Protocol<Client> protocol;

	private ClientSocket tcpSocket;
	private ClientSocket udpSocket;

	/**
	 * Creates a new client with a UDP connection.
	 *
	 * @param port the port to connect to
	 * @param protocol the protocol associated with this client
	 */
	public Client(int port, Protocol<Client> protocol) {
		this.address = "";
		this.tcpPort = 0;
		this.udpPort = port;
		this.protocol = protocol;
	}

	/**
	 * Creates a new client with a TCP connection.
	 *
	 * @param address the address to connect to
	 * @param port the port to connect to
	 * @param protocol the protocol associated with this client
	 */
	public Client(String address, int port, Protocol<Client> protocol) {
		this.address = address;
		this.tcpPort = port;
		this.udpPort = 0;
		this.protocol = protocol;
	}

	/**
	 * Creates a new client with both a TCP and a UDP connection.
	 *
	 * @param address the address for the TCP connection
	 * @param tcpPort the port for the TCP connection
	 * @param udpPort the port for the UDP connection
	 * @param protocol the protocol to associate with this client
	 */
	public Client(String address, int tcpPort, int udpPort, Protocol<Client> protocol) {
		this.address = address;
		this.tcpPort = tcpPort;
		this.udpPort = udpPort;
		this.protocol = protocol;
	}

	private ClientSocket createSocket(TransportProtocol type, int port)
		throws IOException {
		ClientSocket socket = ClientSocketFactory.createSocket(type, this.address, port);

		socket.connect();

		new ClientListener(this, socket).start();
		return socket;
	}

	/**
	 * Tries to connect to the server.
	 *
	 * @throws IOException If connecting to the server fails
	 */
	public void connect() throws IOException {
		if (this.tcpPort != 0) {
			createSocket(TransportProtocol.TCP, this.tcpPort);
			this.onClientConnected();
		}
		if (this.udpPort != 0) {
			createSocket(TransportProtocol.UDP, this.udpPort);
		}
	}

	/**
	 * Gets the session ID.
	 *
	 * @return The session ID
	 */
	public UUID getSessionId() {
		if (tcpSocket != null) {
			return ((TcpClientSocket) tcpSocket).getSessionId();
		}
		return null;
	}

	/**
	 * Sends a message to the server.
	 *
	 * @param message to send to the server.
	 */
	public void sendOutgoingMessage(Message<?> message) {
		try {
			if (message.isUdp()) {
				if (getSessionId() != null) {
					message.assignSource(getSessionId());
				}

				this.udpSocket.sendMessage(message);
			} else {
				this.tcpSocket.sendMessage(message);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Notifies the connected protocol that a new message has been received.
	 *
	 * @param message the received message object
	 */
	public void registerIncomingMessage(Message<?> message) {
		this.protocol.receiveMessage(this, message);
	}

	/**
	 * Notifies the connected protocol that the client has connected to a server and received a
	 * session id.
	 */
	public void onClientConnected() {
		this.protocol.onClientConnect(this, getSessionId());
	}

	/**
	 * Notifies the connected protocol that the client has disconnected from the server.
	 */
	public void onClientDisconnected() {
		this.protocol.onClientDisconnect(this, getSessionId());
	}
}
