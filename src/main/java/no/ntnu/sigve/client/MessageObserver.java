package no.ntnu.sigve.client;


import no.ntnu.sigve.communication.Message;

public interface MessageObserver {
	void update(Message<?> message);
}
