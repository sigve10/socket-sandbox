package no.ntnu.sigve;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import no.ntnu.sigve.client.Client;
import no.ntnu.sigve.communication.Message;
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

	private <T extends Message> T waitForMessage(TestClientProtocol protocol, Class<T> messageType) {
		return messageType.cast(await()
				.atMost(5, TimeUnit.SECONDS)
				.until(protocol::getMessage, messageType::isInstance));
	}

	private Message waitForMessage(TestClientProtocol protocol) {
		return waitForMessage(protocol, Message.class);
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
		client.sendOutgoingMessage(new StringMessage("1", client.getSessionId()));
		StringMessage message = waitForMessage(protocol, StringMessage.class);
		assertEquals("1", message.getPayload());
	}

	@Test
	void testThatMessagesReturnsInOrder() {
		client.sendOutgoingMessage(new StringMessage("1", client.getSessionId()));
		client.sendOutgoingMessage(new StringMessage("2", client.getSessionId()));
		assertEquals("1", waitForMessage(protocol, StringMessage.class).getPayload());
		assertEquals("2", waitForMessage(protocol, StringMessage.class).getPayload());
	}

	@Test
	void testBroadcast() {
		TestClientProtocol clientProtocol = new TestClientProtocol();
		createClient(clientProtocol);
		StringMessage message = new StringMessage("Hello", null);
		server.broadcast(message);
		assertEquals("Hello", waitForMessage(clientProtocol, StringMessage.class).getPayload());
	}

	@Test
	void testRoute() {
		server.route(new StringMessage("Hello", client.getSessionId()));
		assertEquals("Hello", waitForMessage(protocol, StringMessage.class).getPayload());
	}

	@Test
	void testSimultaneousSending() {
		client.sendOutgoingMessage(new StringMessage("1", client.getSessionId()));
		client1.sendOutgoingMessage(new StringMessage("1", client1.getSessionId()));
		assertEquals("1", waitForMessage(protocol, StringMessage.class).getPayload());
		assertEquals("1", waitForMessage(protocol1, StringMessage.class).getPayload());
	}

}
