package com.example;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.client.Client;
import com.example.server.Server;
import com.example.testclasses.TestProtocol;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests.
 */
public class ClientTest {
	Thread serverThread;

	/**
	 * Test.
	 */
	@BeforeEach
	public void initializeServer() {
		serverThread = new Thread(
			() -> {
				Server server = null;
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
			Thread.sleep(100);
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
}
