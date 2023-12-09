package no.ntnu.sigve.sockets;

public class ClientSocketFactory {
	public ClientSocket createSocket(String protocol, String serverAddress, int serverPort) {
		switch (protocol.toUpperCase()) {
			case "TCP":
				return new TcpClientSocket(serverAddress, serverPort);
			case "UDP":
				return new UdpClientSocket(serverAddress, serverPort);
			default:
				throw new IllegalArgumentException("Unsupported protocol: " + protocol);
		}
	}
}
