package no.ntnu.sigve;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import no.ntnu.sigve.server.Server;
import no.ntnu.sigve.testclasses.TestProtocol;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;

public class ServerTest {
	Server server;

	@BeforeAll
	public void initializeServer() {
		try {
			server = new Server(8080, new TestProtocol());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void constructorTest() {
		assertNotNull(server);
	}
}