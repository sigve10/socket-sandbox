package no.ntnu.sigve.communication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import java.io.IOException;
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
 * information to be sent.</li></ul>
 */
@JsonDeserialize()
public class Message implements Serializable {
	private UUID source;
	private UUID destination;

	/**
	 * Creates a new message for the given destination.
	 *
	 * @param source the address of the client from which the message was sent
	 * @param destination the destination of the message
	 */
	@JsonCreator
	public Message(
			@JsonProperty("destination") UUID destination,
			@JsonProperty("source") UUID source
	) {
		this.destination = destination;
		this.source = source;
	}

	/**
	 * Creates a new message for the given destination.
	 *
	 * @param destination the destination of the message
	 */
	public Message(UUID destination) {
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
	@JsonProperty
	public final UUID getSource() {
		return this.source;
	}

	/**
	 * Gets the destination of the message.
	 *
	 * @return the destination of the message
	 */
	@JsonProperty
	public final UUID getDestination() {
		return this.destination;
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

	public static class MessageSerializer extends JsonSerializer<Message> {
		@Override
		public void serialize(Message message, JsonGenerator jgen, SerializerProvider provider) throws IOException {
			jgen.writeStartObject();
			jgen.writeObjectField("class", message.getClass());
			JavaType javaType = provider.constructType(message.getClass());
			JsonSerializer<Object> serializer = BeanSerializerFactory.instance.createSerializer(provider, javaType);
			// this is basically your 'writeAllFields()'-method:
			serializer.unwrappingSerializer(null).serialize(message, jgen, provider);
			jgen.writeEndObject();
		}
	}

	public static class MessageDeserializer extends StdDeserializer<Message> {

		public MessageDeserializer() {
			super(Message.class);
		}

		@Override
		public Message deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
			ObjectNode root = jsonParser.readValueAsTree();
			ObjectMapper mapper = new ObjectMapper();
			JsonNode node = root.remove("class");
			Class<? extends Message> messageClass = mapper.readerFor(new TypeReference<Class<? extends Message>>() {}).readValue(node);

			return mapper.readerFor(messageClass).readValue(root);
		}
	}
}
