package no.ntnu.sigve.testclasses;

import no.ntnu.sigve.communication.Message;
import no.ntnu.sigve.server.Protocol;
import no.ntnu.sigve.server.Server;

/**
 * A class for testing the protocol interface.
 */
public class TestProtocol implements Protocol {
	/**
	 * Class constructor.
	 *
	 */
	public TestProtocol() {
	}

	@Override
	public void receiveMessage(Server server, Message<?> message) {
		switch ((String) message.getPayload()) {
			case "1" -> server.route(message);
			case "2" -> server.route(message);
			default -> {
				//Do nothing
			}
		}
	}
}
