package no.ntnu.sigve.communication;

import java.util.Objects;
import java.util.UUID;

/**
 * A message of unknown typing. When sending messages, any deserialized message will
 */
public final class UnknownMessage extends Message {
    private final String type;
    private final String payload;

    private UnknownMessage(UUID destination, UUID source, String type, String payload) {
        super(destination);

        if (type == null) throw new IllegalArgumentException("String \"type\" cannot be null");

        assignSource(source);
        this.type = type;
        this.payload = payload;
    }

    public static UnknownMessage fromString(String serializedMessage) {
        UnknownMessage message = null;
        if (serializedMessage != null) {
            // NOTE: String#split(String) does not append empty strings at the end of the array for trailing vertical bars
            String[] messageParams = serializedMessage.split("\\|");
            message = new UnknownMessage(
                    messageParams[0].isBlank() ? null : UUID.fromString(messageParams[0]),
                    messageParams[1].isBlank() ? null : UUID.fromString(messageParams[1]),
                    messageParams[2],
                    messageParams.length <= 3 || messageParams[3].isBlank() ? null : messageParams[3]
            );
        }
        return message;
    }

    public String getRawPayload() {
        return payload;
    }

    @Override
    public String serializeContent() {
        return payload;
    }

    @Override
    public String getTypeIdentifier() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UnknownMessage message = (UnknownMessage) o;
        return type.equals(message.type) && Objects.equals(payload, message.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type, payload);
    }
}
