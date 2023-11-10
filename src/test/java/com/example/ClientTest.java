package com.example;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.TestClasses.SecondTestCommand;
import com.example.TestClasses.TestCommand;
import com.example.client.Client;
import com.example.server.Server;
import com.example.server.commands.Command;
import com.example.server.commands.CommandSet;
import com.example.server.commands.CommandSet.CommandSetBuilder;
import com.example.server.handler.Logic;
import java.io.IOException;
import java.security.PublicKey;

import org.junit.After;
import org.junit.Before;
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
				try {
					server = new Server(8080);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				server.setCommandSet(
					new CommandSetBuilder()
					.add("test1", new TestCommand())
					.add("test2", new SecondTestCommand())
					.build()
					);
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
	public  void stopServer() {
		System.out.println("This ran last");
		serverThread.interrupt();
	}

	@Test
	public void negativeConstructorTest1() {
		assertThrows(IOException.class, () -> new Client("localhost", 0));
	}

	@Test
	public void negativeConstructorTest2() {
		assertThrows(IOException.class, () -> new Client("", 8080));
	}

	@Test
	public void constructorTest() {
		assertDoesNotThrow(() -> new Client("localhost", 8080));
	}

	@Test
	public void message() {
		Client client = createClient();

		client.sendOutgoingMessage("test1");
		String message = client.nextIncomingMessage();
		assertTrue(message.equals("test1"));
	}

	@Test
	public void testThatNoMessageReturnsNull(){
		Client client = createClient();
		String message = client.nextIncomingMessage();
		assertTrue(message == null);
	}

	@Test
	public void testThatMessagesReturnsInOrder(){
		boolean success = false;
		Client client = createClient();
		client.sendOutgoingMessage("test1");
		client.sendOutgoingMessage("test2");

		if (
			client.nextIncomingMessage().equals("test1") 
			&& client.nextIncomingMessage().equals("test2")
		) {
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
