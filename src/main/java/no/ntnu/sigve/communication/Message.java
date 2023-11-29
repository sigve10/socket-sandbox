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
public class Message<T extends Serializable> implements Serializable {
	private UUID source;
	private UUID destination;
	private T payload;
	private UUID sessionId;
	private boolean isUdp;

	/**
	 * Creates a new message for the given destination.
	 *
	 * @param destination the destination of the message
	 * @param payload the body of the message
	 */
	public Message(UUID destination, T payload) {
		this.destination = destination;
		this.payload = payload;
	}

	/**
	 * Creates a new message for the given destination.
	 *
	 * @param destination the destination of the message
	 */
	public Message(UUID destination) {
		this(destination, null);
	}

	/**
	 * Assigns this message's source. Should be done server-side.
	 *
	 * @param source the address of the client from which the message was sent
	 */
	public final void assignSource(UUID source) {
		if (this.source == null) {
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
	public final void setPayload(T payload) {
		if (this.payload == null) {
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
	public final T getPayload() {
		return this.payload;
	}

	public UUID getSessionId() {
		return sessionId;
	}

	public void setSessionId(UUID sessionId) {
		this.sessionId = sessionId;
	}

	public boolean isUdp() {
		return isUdp;
	}

	public void setUdp(boolean isUdp) {
		this.isUdp = isUdp;
	}

}
