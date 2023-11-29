package no.ntnu.sigve.communication;

import java.util.UUID;

/**
 * Represents any user of a protocol. This can be a client or a server, or anything that is capable
 * of receiving messages and should use a protocol to handle such events.
 */
public abstract class ProtocolUser {
	private final Protocol protocol;

	/**
	 * Initializes a class as a protocol user.
	 *
	 * @param protocol the protocol to be utilized by this protocol user.
	 */
	protected ProtocolUser(Protocol protocol) {
		this.protocol = protocol;
	}

	/**
	 * An event that should be called whenever this protocol user receives a message.
	 *
	 * @param message the incoming message
	 */
	protected final void onMessageReceived(Message<?> message) {
		this.protocol.receiveMessage(this, message);
	}

	/**
	 * An event that should be called whenever this protocol starts a connection, whether
	 * server-side or client-side.
	 *
	 * @param clientUuid the session ID of the connection
	 */
	protected final void onClientConnect(UUID clientUuid) {
		this.protocol.onClientConnect(this, clientUuid);
	}

	/**
	 * An event that should be called whenever this protocol ends a connection, whether
	 * server-side or client-side.
	 *
	 * @param clientUuid the session ID of the connection
	 */
	protected final void onClientDisconnect(UUID clientUuid) {
		this.protocol.onClientDisconnect(this, clientUuid);
	}
}
