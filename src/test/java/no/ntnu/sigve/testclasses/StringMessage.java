package no.ntnu.sigve.testclasses;

import java.util.Objects;
import java.util.UUID;
import no.ntnu.sigve.communication.Message;

public class StringMessage extends Message {
    public static final String TYPE_IDENTIFIER = "STRING";

    private final String string;

    public StringMessage(UUID destination, String string) {
        super(destination);
        this.string = string;
    }

    public String getString() {
        return string;
    }

    @Override
    public String serializeContent() {
        return string;
    }

    @Override
    public String getTypeIdentifier() {
        return TYPE_IDENTIFIER;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        StringMessage that = (StringMessage) o;
        return Objects.equals(string, that.string);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), string);
    }
}
