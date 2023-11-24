package no.ntnu.sigve.communication;

import java.util.UUID;

public class UuidMessage extends Message<UUID> {
	public UuidMessage(UUID payload) {
		super(null, payload);
	}
}
