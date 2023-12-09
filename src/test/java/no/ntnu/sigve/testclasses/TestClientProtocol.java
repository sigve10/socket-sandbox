package no.ntnu.sigve.testclasses;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import no.ntnu.sigve.client.Client;
import no.ntnu.sigve.communication.Message;
import no.ntnu.sigve.communication.Protocol;

public class TestClientProtocol implements Protocol<Client> {
	private final List<Message<?>> messages;

	public TestClientProtocol() {
		messages = new ArrayList<>();
	}

	@Override
	public void receiveMessage(Client caller, Message<?> message) {
		this.messages.add(message);
	}

	@Override
	public void onClientConnect(Client caller, UUID clientId) {

	}

	@Override
	public void onClientDisconnect(Client caller, UUID clientId) {

	}

	public Message<?> getMessage() {
		return messages.isEmpty() ? null : messages.remove(0);
	}
}
