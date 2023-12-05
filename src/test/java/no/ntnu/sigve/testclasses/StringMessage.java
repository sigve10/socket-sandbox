package no.ntnu.sigve.testclasses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import no.ntnu.sigve.communication.Message;

public class StringMessage extends Message {
    private final String payload;

    @JsonCreator
    public StringMessage(
            @JsonProperty("payload") String payload,
            @JsonProperty("destination") UUID destination,
            @JsonProperty("source") UUID source
    ) {
        super(destination, source);
        this.payload = payload;
    }

    public StringMessage(String payload, UUID destination) {
        this(payload, destination, null);
    }

    @JsonProperty
    public String getPayload() {
        return payload;
    }
}
