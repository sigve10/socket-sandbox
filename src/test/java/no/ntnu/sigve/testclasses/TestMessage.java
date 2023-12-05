package no.ntnu.sigve.testclasses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import no.ntnu.sigve.MessageMapper;
import no.ntnu.sigve.communication.Message;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class TestMessage {

	@Test
	void TestMessageSerialization() throws IOException {
		ObjectMapper json = new MessageMapper();

		UUID destination = UUID.randomUUID();
		UUID source = UUID.randomUUID();

		FunkyMessage.FunkyObject originalPayload = new FunkyMessage.FunkyObject("mordi B)", 69420);
		FunkyMessage originalMessage = new FunkyMessage(originalPayload, destination, source);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		json.writer().writeValues(baos).write(originalMessage);

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		MappingIterator<Message> messages = json.readerFor(Message.class).readValues(bais);
		Message deserializedMessage = messages.hasNext() ? messages.next() : null;

		assertNotNull(deserializedMessage);
		FunkyMessage funkyMessage = assertInstanceOf(FunkyMessage.class, deserializedMessage);
		assertEquals(originalMessage, funkyMessage);
	}

	private static class FunkyMessage extends Message {
		private final FunkyObject funkyObject;

		@JsonCreator
		public FunkyMessage(
				@JsonProperty("funkyObject") FunkyObject funkyObject,
				@JsonProperty("destination") UUID destination,
				@JsonProperty("source") UUID source
		) {
			super(destination, source);
			this.funkyObject = funkyObject;
		}

		@JsonProperty
		public FunkyObject getFunkyObject() {
			return funkyObject;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			if (!super.equals(o)) return false;
			FunkyMessage that = (FunkyMessage) o;
			return Objects.equals(funkyObject, that.funkyObject);
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), funkyObject);
		}

		private record FunkyObject(String someString, int someInt) implements Serializable {}
	}
}
