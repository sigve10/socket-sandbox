package no.ntnu.sigve;


import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import no.ntnu.sigve.client.Client;
import no.ntnu.sigve.server.Server;
import no.ntnu.sigve.testclasses.TestProtocol;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests.
 */
public class ClientTest {
	private static Thread serverThread;
	private static Server server = null;

	private String waitForMessage(Client client) {
		return await()
				.atMost(5, TimeUnit.SECONDS)
				.until(client::nextIncomingMessage, Objects::nonNull);
	}

	/**
	 * Test.
	 */
	@BeforeAll
	public static void initializeServer() {
		serverThread = new Thread(
			() -> {
				TestProtocol protocol = new TestProtocol();
				try {
					server = new Server(8080, protocol);
				} catch (IOException e) {
					e.printStackTrace();
				}
				protocol.setServer(server);
				server.start();
				System.out.println("This ran first");
			}
		);
		serverThread.setName("Server thread");
		serverThread.start();
	}

	/**
	 * Test.
	 */
	@AfterEach
	public void stopServer() {
		System.out.println("This ran last");
		serverThread.interrupt();
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
		Client client = createClient();
		client.sendOutgoingMessage("test1");
		assertEquals("test1", waitForMessage(client));
	}

	@Test
	void testThatNoMessageReturnsNull() {
		Client client = createClient();
		String message = client.nextIncomingMessage();
		assertNull(message);
	}

	@Test
	void testThatMessagesReturnsInOrder() {
		Client client = createClient();
		client.sendOutgoingMessage("test1");
		client.sendOutgoingMessage("test2");
		assertEquals("test1", waitForMessage(client));
		assertEquals("test2", waitForMessage(client));
	}

	/**
	 * Creates a client for test purposes.
	 *
	 * @return A client to run test on.
	 */
	public Client createClient() {
		Client client = null;
		try {
			client = new Client("localhost", 8080);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return client;
	}

	@Test
	void testBroadcast() {
		Client client = createClient();
		server.broadcast("Hello");
		assertEquals("Hello", waitForMessage(client));
	}

	@Test
	void testRoute() throws UnknownHostException {
		Client client = createClient();
		server.route(InetAddress.getByName("localhost"), "Hello");
		assertEquals("Hello", waitForMessage(client));
	}
}
