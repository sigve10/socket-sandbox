package no.ntnu.sigve.server;

import java.io.Serializable;
import no.ntnu.sigve.communication.Message;

/**
 * An interface representing a protocol responsible for handling raw client/server messages.
 * When creating a {@link Server}, a protocol should be included to handle all incoming messages.
 */
public interface Protocol {
	/**
	 * Accepts a message from a {@link ServerConnection} and 
	 * interprets it to do server-side actions.
	 *
	 * @param message the raw message from the client.
	 */
	void receiveMessage(Message<? extends Serializable> message);
}
