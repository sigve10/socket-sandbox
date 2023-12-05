package no.ntnu.sigve.communication;

import java.util.Objects;
import java.util.UUID;

/**
 * A specific message used for providing a connected client with its server-side ID.
 */
public class UuidMessage extends Message {
	public static final String TYPE_IDENTIFIER = "UUID";

	private final UUID id;

	/**
	 * Creates a new UUIDMessage.
	 *
	 * @param id the UUID to be transmitted.
	 */
	public UuidMessage(UUID id) {
		super(null);
		this.id = id;
	}

	@Override
	public String serializeContent() {
		return id.toString();
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
		UuidMessage that = (UuidMessage) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), id);
	}
}
