package no.ntnu.sigve.testclasses;

import java.util.UUID;

import no.ntnu.sigve.client.Client;
import no.ntnu.sigve.communication.Message;
import no.ntnu.sigve.communication.Protocol;

public class TestClientProtocol implements Protocol<Client> {
	public Message<?> message;

	@Override
	public void receiveMessage(Client caller, Message<?> message) {
		this.message = message;
	}

	@Override
	public void onClientConnect(Client caller, UUID clientId) {

	}

	@Override
	public void onClientDisconnect(Client caller, UUID clientId) {

	}

	public Message<?> getMessage() {
		return message;
	}
}
