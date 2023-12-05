package no.ntnu.sigve.communication;

import java.util.UUID;

/**
 * A protocol that can handle events fired by a protocol user.
 */
public interface Protocol<T extends ProtocolUser> {

	/**
	 * An event that is fired when a {@link Message} arrives at a {@link ProtocolUser}. Messages
	 * have unknown payloads, so message casting should be implemented where necessary.
	 *
	 * @param caller the {@link ProtocolUser} that fired the event
	 * @param message a {@link Message} of unknown payload.
	 */
	void receiveMessage(T caller, Message message);

	/**
	 * An event that is fired when a new connection is established with this {@link ProtocolUser}.
	 *
	 * @param caller the {@link ProtocolUser} that fired the event
	 * @param clientId the UUID of the established connection
	 */
	void onClientConnect(T caller, UUID clientId);

	/**
	 * An event that is fired when a connection is severed from this {@link ProtocolUser}.
	 *
	 * @param caller the {@link ProtocolUser} that fired the event
	 * @param clientId the UUID of the removed connection
	 */
	void onClientDisconnect(T caller, UUID clientId);
}
