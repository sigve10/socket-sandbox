package no.ntnu.sigve;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import no.ntnu.sigve.client.Client;
import no.ntnu.sigve.server.Server;
import no.ntnu.sigve.testclasses.StringMessage;
import no.ntnu.sigve.testclasses.TestClientProtocol;
import no.ntnu.sigve.testclasses.TestProtocol;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Tests.
 */
class ClientTest {
	private Server server = null;
	private Client client;
	private TestClientProtocol protocol;
	private Client client1;
	private TestClientProtocol protocol1;

	/**
	 * Creates a client for test purposes.
	 *
	 * @return A client to run test on.
	 */
	public Client createClient(TestClientProtocol protocol) {
		Client client = new Client("localhost", 8080, protocol);
		try {
			client.connect();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return client;
	}

	private StringMessage waitForStringMessage(TestClientProtocol protocol) {
		return (StringMessage) await()
				.atMost(5, TimeUnit.SECONDS)
				.until(protocol::getMessage, message -> message instanceof StringMessage);
	}

	/**
	 * Test.
	 */
	@BeforeEach
	public void initializeServer() {
		TestProtocol serverProtocol = new TestProtocol();
		try {
			server = new Server(8080, serverProtocol);
		} catch (IOException e) {
			e.printStackTrace();
		}
		server.start();
		System.out.println("This ran first");
		this.protocol = new TestClientProtocol();
		client = createClient(protocol);
		this.protocol1 = new TestClientProtocol();
		client1 = createClient(protocol1);
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
		client.sendOutgoingMessage(new StringMessage(client.getSessionId(), "1"));
		assertEquals("1", waitForStringMessage(protocol).getString());
	}

	@Test
	void testThatMessagesReturnsInOrder() {
		client.sendOutgoingMessage(new StringMessage(client.getSessionId(), "1"));
		client.sendOutgoingMessage(new StringMessage(client.getSessionId(), "2"));
		assertEquals("1", waitForStringMessage(protocol).getString());
		assertEquals("2", waitForStringMessage(protocol).getString());
	}

	@Test
	void testBroadcast() {
		TestClientProtocol clientProtocol = new TestClientProtocol();
		createClient(clientProtocol);
		StringMessage message = new StringMessage(null, "Hello");
		server.broadcast(message);
		assertEquals("Hello", waitForStringMessage(clientProtocol).getString());
	}

	@Test
	void testRoute() {
		server.route(new StringMessage(client.getSessionId(), "Hello"));
		assertEquals("Hello", waitForStringMessage(protocol).getString());
	}

	@Test
	void testSimultaneousSending() {
		client.sendOutgoingMessage(new StringMessage(client.getSessionId(), "1"));
		client1.sendOutgoingMessage(new StringMessage(client1.getSessionId(), "1"));
		assertEquals("1", waitForStringMessage(protocol).getString());
		assertEquals("1", waitForStringMessage(protocol1).getString());
	}

}
