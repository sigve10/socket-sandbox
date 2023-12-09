package no.ntnu.sigve.sockets;

/**
 * Factory for creating {@link ClientSockets}. Can create TCP and UDP sockets.
 */
public class ClientSocketFactory {
	private ClientSocketFactory() {}

	/**
	 * Creates a socket with the given protocol.
	 *
	 * @param protocol the protocol the socket should use
	 * @param serverAddress the address the socket should connect to. Ignored if the socket is UDP
	 * @param serverPort the port address the socket should connect to
	 * @return the created socket
	 */
	public static ClientSocket createSocket(
		TransportProtocol protocol, String serverAddress, int serverPort) {
		switch (protocol) {
			case TCP:
				return new TcpClientSocket(serverAddress, serverPort);
			case UDP:
				return new UdpClientSocket(serverPort);
			default:
				throw new IllegalArgumentException("Unsupported protocol: " + protocol);
		}
	}
}
