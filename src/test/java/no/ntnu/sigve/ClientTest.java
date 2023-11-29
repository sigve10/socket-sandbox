package no.ntnu.sigve;


import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import no.ntnu.sigve.client.Client;
import no.ntnu.sigve.communication.Message;
import no.ntnu.sigve.communication.Protocol;
import no.ntnu.sigve.server.Server;
import no.ntnu.sigve.testclasses.TestClientProtocol;
import no.ntnu.sigve.testclasses.TestProtocol;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests.
 */
public class ClientTest {
	private static Server server = null;
	private static Client client;
	private static Client client1;
	private static TestClientProtocol protocol;

	/**
	 * Creates a client for test purposes.
	 *
	 * @return A client to run test on.
	 */
	public static Client createClient() {
		protocol = new TestClientProtocol();
		Client client = new Client("localhost", 8080, protocol);
		try {
			client.connect();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return client;
	}

	private Message<?> waitForMessage(Client client) {
		return await()
				.atMost(5, TimeUnit.SECONDS)
				.until(protocol::getMessage, Objects::nonNull);
	}

	/**
	 * Test.
	 */
	@BeforeEach
	public void initializeServer() {
		TestProtocol protocol = new TestProtocol();
		try {
			server = new Server(8080, protocol);
		} catch (IOException e) {
			e.printStackTrace();
		}
		server.start();
		System.out.println("This ran first");
		client = createClient();
		client1 = createClient();
	}

	/**
	 * Test.
	 */
	@AfterEach
	public void stopServer() {
		System.out.println("This ran last");
		server.close();
	}

	@Test
	void negativeConnectionTest1() {
		Client illegalClient = new Client("localhost", 0, protocol);
		assertThrows(IOException.class, illegalClient::connect);

	}

	@Test
	void constructorTest() {
		assertDoesNotThrow(() -> new Client("localhost", 8080, protocol));
	}

	@Test
	void message() {
		client.sendOutgoingMessage(new Message<>(client.getSessionId(), "1"));
		assertEquals("1", waitForMessage(client).getPayload());
	}

	@Test
	void testThatMessagesReturnsInOrder() {
		client.sendOutgoingMessage(new Message<>(client.getSessionId(), "1"));
		client.sendOutgoingMessage(new Message<>(client.getSessionId(), "2"));
		assertEquals("1", waitForMessage(client).getPayload());
		assertEquals("2", waitForMessage(client).getPayload());
	}

	@Test
	void testBroadcast() {
		Client client = createClient();
		Message<String> message = new Message<>(null);
		message.setPayload("Hello");
		server.broadcast(message);
		assertEquals("Hello", waitForMessage(client).getPayload());
	}

	@Test
	void testRoute() {
		server.route(new Message<>(client.getSessionId(), "Hello"));
		client.registerIncomingMessage(new Message<>(client.getSessionId(), "Hello"));
		assertEquals("Hello", waitForMessage(client).getPayload());
	}
	@Test
	void testSimultaneousSending() {
		client.sendOutgoingMessage(new Message<Serializable>(client.getSessionId(), "1"));
		client1.sendOutgoingMessage(new Message<Serializable>(client1.getSessionId(), "1"));
		assertEquals("1", waitForMessage(client).getPayload());
		assertEquals("1", waitForMessage(client1).getPayload());
	}

}
