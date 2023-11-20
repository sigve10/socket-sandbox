package no.ntnu.sigve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import no.ntnu.sigve.client.Client;
import no.ntnu.sigve.server.Server;
import no.ntnu.sigve.testclasses.TestProtocol;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests.
 */
public class ClientTest {
	Thread serverThread;
	Server server = null;

	/**
	 * Test.
	 */
	@BeforeEach
	public void initializeServer() {
		serverThread = new Thread(
			() -> {
				TestProtocol protocol = new TestProtocol();
				try {
					server = new Server(8080, protocol);
				} catch (IOException e) {
					e.printStackTrace();
				}
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
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		String message = client.nextIncomingMessage();
		assertTrue(message.equals("test1"));
	}

	@Test
	void testThatNoMessageReturnsNull() {
		Client client = createClient();
		String message = client.nextIncomingMessage();
		assertNull(message);
	}

	@Test
	void testThatMessagesReturnsInOrder() {
		boolean success = false;
		Client client = createClient();
		client.sendOutgoingMessage("test1");
		client.sendOutgoingMessage("test2");
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		String t1 = client.nextIncomingMessage();
		String t2 = client.nextIncomingMessage();


		System.out.println(t1 + " , " + t2);

		if (t1.equals("test1") && t2.equals("test2")) {
			success = true;
		}

		assertTrue(success);

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
		this.server.broadcast("Hello");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals("Hello", client.nextIncomingMessage());
	}

	@Test
	void testRoute() throws UnknownHostException {
		Client client = createClient();
		this.server.route(InetAddress.getByName("localhost"), "Hello");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals("Hello", client.nextIncomingMessage());
	}
}
