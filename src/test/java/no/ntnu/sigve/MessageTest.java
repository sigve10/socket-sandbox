package no.ntnu.sigve;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import no.ntnu.sigve.communication.UnknownMessage;
import org.junit.jupiter.api.Test;

class MessageTest {

	private UnknownMessage runSerialization(UnknownMessage message) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		new DataOutputStream(baos).writeUTF(message.getSerialized());

		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));

		return UnknownMessage.fromString(dis.readUTF());
	}

	private void serializationTest(UnknownMessage message) throws IOException {
		assertEquals(message, runSerialization(message));
	}

	@Test
	void TestMessageSerialization() throws IOException {
		UUID destination = UUID.randomUUID();
		UUID source = UUID.randomUUID();

		serializationTest(UnknownMessage.fromString(String.format("||UUID|%s", destination)));
		serializationTest(UnknownMessage.fromString(String.format("%s|%s|Some type lol|A very funky payload B)", destination, source)));
		serializationTest(UnknownMessage.fromString(String.format("%s||Type with no payload or source|", destination)));
	}
}
