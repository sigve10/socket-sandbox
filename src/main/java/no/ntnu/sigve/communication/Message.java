package no.ntnu.sigve.communication;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a message sent between server and client.
 *
 * <ul><li>{@link Message#destination Destination} is the client to which the message should be
 * sent.</li>
 * <li>{@link Message#source Source} is the client from which the message was sent. This should be
 * set server-side to ensure global recognition of the source.</li>
 * <li>{@link Message#serializeContent()} takes the content of a message, and serializes the
 * information to be sent.</li></ul>
 */
public abstract class Message implements Serializable {
    private UUID source;
    private UUID destination;

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
     * Serialized this message's content into a payload.
     *
     * @return this message's payload
     */
    public abstract String serializeContent();

    /**
     * The identifier used to identify this type of message from a serialized string.
     * <p>The {@code UUID} identifier is reserved for {@link UuidMessage},
     * and should not be used by any other messages.</p>
     * <p>This should never return {@code null}</p>
     *
     * @return The identifier for this message
     */
    public abstract String getTypeIdentifier();

    public final String getSerialized() {
        String payload = serializeContent();
        return String.format("%s|%s|%s|%s",
                destination == null ? "" : destination,
                source == null ? "" : source,
                getTypeIdentifier(),
                payload == null ? "" : payload
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(source, message.source) && Objects.equals(destination, message.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, destination);
    }
}
