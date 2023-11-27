package no.ntnu.sigve.testclasses;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.UUID;
import no.ntnu.sigve.communication.Message;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class TestMessage {

	@Test
	void TestMessageSerialization() throws IOException, ClassNotFoundException {
		UUID destination = UUID.randomUUID();
		UUID source = UUID.randomUUID();

		Message<FunkyObject> originalMessage = new Message<>(destination);
		originalMessage.assignSource(source);
		FunkyObject originalPayload = new FunkyObject("mordi B)", 69420);
		originalMessage.setPayload(originalPayload);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		new ObjectOutputStream(baos).writeObject(originalMessage);

		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));

		Message<?> deserializedMessage = (Message<?>) ois.readObject();

		assertTrue(deserializedMessage.getPayload() instanceof FunkyObject);
		assertEquals(originalPayload, deserializedMessage.getPayload());
	}

	private record FunkyObject(String someString, int someInt) implements Serializable {}
}
