package no.ntnu.sigve.communication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.UUIDDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.ser.std.UUIDSerializer;
import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

/**
 * Represents a message sent between server and client.
 * 
 * <ul><li>{@link Message#destination Destination} is the client to which the message should be
 * sent.</li>
 * <li>{@link Message#source Source} is the client from which the message was sent. This should be
 * set server-side to ensure global recognition of the source.</li>
 * <li>{@link Message#payload Payload} is the content of the message. A string containing the
 * information to be sent.</li></ul>
 */
@JsonSerialize(using = Message.MessageSerializer.class)
public class Message<T extends Serializable> implements Serializable {
	@JsonSerialize(using = UUIDSerializer.class)
	@JsonDeserialize(using = UUIDDeserializer.class)
	private UUID source;

	@JsonSerialize(using = UUIDSerializer.class)
	@JsonDeserialize(using = UUIDDeserializer.class)
	private UUID destination;

	private T payload;

	/**
	 * Creates a new message for the given destination.
	 *
	 * @param source the address of the client from which the message was sent
	 * @param destination the destination of the message
	 * @param payload the body of the message
	 */
	@JsonCreator
	public Message(
			@JsonProperty("source") UUID source,
			@JsonProperty("destionation") UUID destination,
			@JsonProperty("payload") T payload) {
		System.out.println("owo");
		this.source = source;
		this.destination = destination;
		this.payload = payload;
	}

	/**
	 * Creates a new message for the given destination.
	 *
	 * @param destination the destination of the message
	 * @param payload the body of the message
	 */
	public Message(UUID destination, T payload) {
		this.destination = destination;
		this.payload = payload;
	}

	/**
	 * Creates a new message for the given destination.
	 *
	 * @param destination the destination of the message
	 */
	public Message(UUID destination) {
		this(destination, null);
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
	 * Sets the message's payload.
	 *
	 * @param payload the body of the message
	 */
	public final void setPayload(T payload) {
		if (this.payload == null) {
			this.payload = payload;
		} else {
			System.err.println("Could not set payload: Message already has a payload.");
		}
	}

	/**
	 * Gets this message's payload.
	 *
	 * @return this message's payload
	 */
	public final T getPayload() {
		return this.payload;
	}

	public static class MessageSerializer extends StdSerializer<Message<?>> {

		public MessageSerializer() {
			this(null);
		}

		public MessageSerializer(Class<Message<?>> t) {
			super(t);
		}

		@Override
		public void serialize(Message<?> message, JsonGenerator gen, SerializerProvider provider) throws IOException {
			gen.writeStartObject();
			gen.writeStringField("source", message.source == null ? null : message.source.toString());
			gen.writeStringField("destination", message.destination == null ? null : message.destination.toString());
			gen.writeObjectField("destination", message.getPayload());
			gen.writeEndObject();
		}
	}
}
