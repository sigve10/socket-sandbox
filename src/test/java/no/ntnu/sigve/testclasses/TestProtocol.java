package no.ntnu.sigve.testclasses;

import java.util.UUID;
import no.ntnu.sigve.communication.Message;
import no.ntnu.sigve.communication.Protocol;
import no.ntnu.sigve.communication.UnknownMessage;
import no.ntnu.sigve.server.Server;

/**
 * A class for testing the protocol interface.
 */
public class TestProtocol implements Protocol<Server> {
	/**
	 * Class constructor.
	 */
	public TestProtocol() {}

	@Override
	public Message resolveMessage(Server caller, UnknownMessage message) {
		Message resolvedMessage;
		if (StringMessage.TYPE_IDENTIFIER.equals(message.getTypeIdentifier())) {
			resolvedMessage = new StringMessage(message.getDestination(), message.getRawPayload());
		} else {
			resolvedMessage = message;
		}
		return resolvedMessage;
	}

	@Override
	public void receiveMessage(Server server, Message message) {
		if (message instanceof StringMessage stringMessage) {
			switch (stringMessage.getString()) {
				case "1" -> server.route(message);
				case "2" -> server.route(message);
				default -> {
					//Do nothing
				}
			}
		}
	}

	@Override
	public void onClientConnect(Server caller, UUID clientId) {
	}

	@Override
	public void onClientDisconnect(Server caller, UUID clientId) {
	}
}
