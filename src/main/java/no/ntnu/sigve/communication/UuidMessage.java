package no.ntnu.sigve.communication;

import java.util.UUID;

/**
 * A specific message used for providing a connected client with its server-side ID.
 */
public class UuidMessage extends Message<UUID> {
	/**
	 * Creates a new UUIDMessage.
	 *
	 * @param payload the UUID to be transmitted.
	 */
	public UuidMessage(UUID payload) {
		super(null, payload);
	}
}
