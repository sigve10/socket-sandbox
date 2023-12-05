package no.ntnu.sigve.communication;

import java.util.UUID;

/**
 * A protocol that can handle events fired by a protocol user.
 */
public interface Protocol<T extends ProtocolUser> {
    /**
     * When a {@link ProtocolUser} receives data & deserializes it into an {@link UnknownMessage},
     * this method is called to try to resolve the specific type of message.
     *
     * @param caller  the {@link ProtocolUser} that fired the event
     * @param message an {@link UnknownMessage} to be resolved
     * @return The resolved message. It is legal for this to be the {@link UnknownMessage} initially received
     * @see #receiveMessage(ProtocolUser, Message)
     */
    Message resolveMessage(T caller, UnknownMessage message);

    /**
     * An event that is fired when a {@link Message} arrives at a {@link ProtocolUser}. Messages
     * have unknown payloads, so message casting should be implemented where necessary.
     *
     * @param caller  the {@link ProtocolUser} that fired the event
     * @param message a possibly resolved {@link Message},
     *                although there is no guarantee that this isn't an {@link UnknownMessage}
     * @see #resolveMessage(ProtocolUser, UnknownMessage)
     */
    void receiveMessage(T caller, Message message);

    /**
     * An event that is fired when a new connection is established with this {@link ProtocolUser}.
     *
     * @param caller   the {@link ProtocolUser} that fired the event
     * @param clientId the UUID of the established connection
     */
    void onClientConnect(T caller, UUID clientId);

    /**
     * An event that is fired when a connection is severed from this {@link ProtocolUser}.
     *
     * @param caller   the {@link ProtocolUser} that fired the event
     * @param clientId the UUID of the removed connection
     */
    void onClientDisconnect(T caller, UUID clientId);
}
