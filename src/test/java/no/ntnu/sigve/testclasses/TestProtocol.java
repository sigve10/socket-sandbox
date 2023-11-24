package no.ntnu.sigve.testclasses;

import java.io.Serializable;

import no.ntnu.sigve.communication.Message;
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
	public void receiveMessage(Message<? extends Serializable> message) {
		switch ((String) message.getPayload()) {
			case "1" -> server.route(address, "test1");
			case "2" -> server.route(address, "test2");
      default -> {
        //Do nothing
      }
    }
	}
}
