package no.ntnu.sigve;


import java.io.IOException;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import no.ntnu.sigve.client.Client;
import no.ntnu.sigve.communication.Message;
import no.ntnu.sigve.server.Server;
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
	private static Thread serverThread;
	private static Server server = null;
	private static Client client;

	/**
	 * Creates a client for test purposes.
	 *
	 * @return A client to run test on.
	 */
	public static Client createClient() {
		Client client = null;
		try {
			client = new Client("localhost", 8080);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return client;
	}

	private Message<? extends Serializable> waitForMessage(Client client) {
		return await()
				.atMost(5, TimeUnit.SECONDS)
				.until(client::nextIncomingMessage, Objects::nonNull);
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
    protocol.setServer(server);
		server.start();
		System.out.println("This ran first");
		client = createClient();
	}

	/**
	 * Test.
	 */
	@AfterEach
	public void stopServer() {
		System.out.println("This ran last");
		client.close();
		server.close();
	}

	@Test
	void negativeConstructorTest1() {
		assertThrows(IOException.class, () -> new Client("localhost", 0));

	}

	@Test
	void positiveConstructorTest2() {
		assertDoesNotThrow(() -> new Client(null, 8080));
	}

	@Test
	void constructorTest() {
		assertDoesNotThrow(() -> new Client("localhost", 8080));
	}

	@Test
	void message() {
		client.sendOutgoingMessage(new Message<>(client.getSessionId(), "test1"));
		assertEquals("test1", waitForMessage(client).getPayload());
	}

	@Test
	void testThatNoMessageReturnsNull() {
		Message<?> message = client.nextIncomingMessage();
		assertNull(message);
	}

	@Test
	void testThatMessagesReturnsInOrder() {
		client.sendOutgoingMessage(new Message<>(client.getSessionId(), "test1"));
		client.sendOutgoingMessage(new Message<>(client.getSessionId(), "test2"));
		assertEquals("test1", waitForMessage(client).getPayload());
		assertEquals("test2", waitForMessage(client).getPayload());
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
	void testRoute() throws UnknownHostException {
		server.route(new Message<>(client.getSessionId(), "Hello"));
		assertEquals("Hello", waitForMessage(client).getPayload());
	}
}
