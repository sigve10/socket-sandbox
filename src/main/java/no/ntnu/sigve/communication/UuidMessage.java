package no.ntnu.sigve.communication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.UUID;

public class UuidMessage extends Message {
	private final UUID id;

	@JsonCreator
	public UuidMessage(
			@JsonProperty("id") UUID id,
			@JsonProperty("destination") UUID destination,
			@JsonProperty("source") UUID source
	) {
		super(destination, source);
		this.id = id;
	}

	public UuidMessage(UUID id) {
		this(id, null, null);
	}

	public UUID getId() {
		return id;
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
