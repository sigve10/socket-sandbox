package no.ntnu.sigve.testclasses;

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
	public void receiveMessage(String message) {
		switch (message) {
			case "1":
				
				break;
		
			case "2":


			default:
				break;
		}
	}
	
}
