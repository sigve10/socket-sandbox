package no.ntnu.sigve.testclasses;

import java.net.InetAddress;

import no.ntnu.sigve.server.Protocol;
import no.ntnu.sigve.server.Server;
/**
 * A class for testing the protocol interface.
 */

public class TestProtocol implements Protocol {
	Server server;

	/**
	 * Class constructor.
	 *
	 */
	public TestProtocol() {
	}

	public void setServer(Server server) {
		this.server = server;
	}

	@Override
	public void receiveMessage(String message, InetAddress address) {
		switch (message) {
			case "test1":
				server.route(address, "test1");

			case "test2":
				server.route(address, "test2");

			default:
				break;
		}
	}
	
}
