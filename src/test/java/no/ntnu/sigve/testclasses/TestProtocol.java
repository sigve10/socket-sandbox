package no.ntnu.sigve.testclasses;

import java.io.Serializable;

import no.ntnu.sigve.communication.Message;
import no.ntnu.sigve.server.Protocol;
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
	public void receiveMessage(Message<? extends Serializable> message) {
		switch ((String) message.getPayload()) {
			case "1":

				break;

			case "2":


			default:
				break;
		}
	}
}
