package no.ntnu.sigve.sockets;

public class ClientSocketFactory {
	private ClientSocketFactory() {}

	public static ClientSocket createSocket(TransportProtocol protocol, String serverAddress, int serverPort) {
		switch (protocol) {
			case TCP:
				return new TcpClientSocket(serverAddress, serverPort);
			case UDP:
				return new UdpClientSocket(serverAddress, serverPort);
			default:
				throw new IllegalArgumentException("Unsupported protocol: " + protocol);
		}
	}
}
