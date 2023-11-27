package no.ntnu.sigve.communication;

import java.io.Serializable;
import java.util.UUID;

/**
 * Represents an acknowledgment message sent by the server to confirm the receipt
 * and processing of a specific message from a client.
 * This class extends the generic Message class and is used to acknowledge messages
 * with a matching session ID.
 *
 * @param <T> The type of payload associated with the acknowledgment message.
 */
public class AckMessage extends Message<Serializable> {

    /**
     * Constructs a new AckMessage with the given session ID to acknowledge a message.
     *
     * @param messageIdToAck The UUID of the message to acknowledge.
     */
    public AckMessage(UUID messageIdToAck) {
        super(null, null);
        this.setSessionId(messageIdToAck); 
    }

    /**
     * Checks whether this acknowledgment message is intended for a specific message.
     *
     * @param message The message to compare the session ID with.
     * @return true if this acknowledgment is for the provided message, false otherwise.
     */
    public boolean isAcknowledgementFor(Message<?> message) {
        return this.getSessionId().equals(message.getSessionId());
    }
}
