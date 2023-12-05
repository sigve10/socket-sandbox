package no.ntnu.sigve;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import no.ntnu.sigve.server.Server;
import no.ntnu.sigve.testclasses.TestProtocol;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ServerTest {
	private static Server server;

	@BeforeAll
	public static void initializeServer() {
		try {
			server = new Server(8080, new TestProtocol());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	void constructorTest() {
		assertNotNull(server);
	}
}
