package no.ntnu.sigve.communication;

import java.io.Serializable;
import java.util.UUID;

/**
 * Represents a message sent between server and client.
 * 
 * <ul><li>{@link Message#destination Destination} is the client to which the message should be
 * sent.</li>
 * <li>{@link Message#source Source} is the client from which the message was sent. This should be
 * set server-side to ensure global recognition of the source.</li>
 * <li>{@link Message#payload Payload} is the content of the message. A string containing the
 * information to be sent.</li></ul>
 */
public class Message implements Serializable {
	private UUID source;
	private UUID destination;
	private String payload;

	/**
	 * Creates a new message for the given destination.
	 *
	 * @param destination the destination of the message
	 */
	protected Message(UUID destination) {
		this.destination = destination;
	}

	/**
	 * Assigns this message's source. Should be done server-side.
	 *
	 * @param source the address of the client from which the message was sent
	 */
	public final void assignSource(UUID source) {
		if (this.source != null) {
			this.source = source;
		} else {
			System.err.println("Could not change source: Message already has a source.");
		}
	}

	/**
	 * Gets the source of the message.
	 *
	 * @return the source of the message
	 */
	public final UUID getSource() {
		return this.source;
	}

	/**
	 * Gets the destination of the message.
	 *
	 * @return the destination of the message
	 */
	public final UUID getDestination() {
		return this.destination;
	}

	/**
	 * Sets the message's payload.
	 *
	 * @param payload the body of the message
	 */
	protected final void setPayload(String payload) {
		if (this.payload != null) {
			this.payload = payload;
		} else {
			System.err.println("Could not set payload: Message already has a payload.");
		}
	}

	/**
	 * Gets this message's payload.
	 *
	 * @return this message's payload
	 */
	public final String getPayload() {
		return this.payload;
	}
}
