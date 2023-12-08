package no.ntnu.sigve.testclasses;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import no.ntnu.sigve.client.Client;
import no.ntnu.sigve.communication.Message;
import no.ntnu.sigve.server.Server;
import no.ntnu.sigve.server.Protocol;
import no.ntnu.sigve.sockets.ClientSocketFactory;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class ServerClientConnectionTest {
    private Server server;
    private Client client;
    private final int tcpPort = 12345;
    private final int udpPort = 12347;
    private final String serverAddress = "localhost";
    private TestProtocol protocol;

    @BeforeEach
    void setUp() throws IOException {
        protocol = new TestProtocol();
        server = new Server(tcpPort, udpPort, protocol);
        server.start();

        client = new Client(serverAddress, tcpPort, udpPort, new ClientSocketFactory());
        client.connect();
    }

    @AfterEach
    void tearDown() throws IOException, InterruptedException {
        client.close();
        server.close();
    }

    @Test
    void testTcpConnectionAndMessage() throws IOException, InterruptedException {
        // Prepare a TCP message
        Message<String> tcpMessage = new Message<>(client.getSessionId(), "Hello Server via TCP");
        tcpMessage.setUdp(false);

        // Send message and wait for it to be processed
        client.sendOutgoingMessage(tcpMessage);

        // Assert that the server received the message
        Message<?> receivedMessage = protocol.getMessage(1, TimeUnit.SECONDS);
        assertNotNull(receivedMessage, "No message received in time");
        assertEquals("Hello Server via TCP", receivedMessage.getPayload());
    }

    @Test
    void testUdpConnectionAndMessage() throws IOException, InterruptedException {
        // Prepare a UDP message
        Message<String> udpMessage = new Message<>(client.getSessionId(), "Hello Server via UDP");
        udpMessage.setUdp(true);

        client.sendOutgoingMessage(udpMessage);

        Message<?> receivedMessage = protocol.getMessage(1, TimeUnit.SECONDS);
        assertNotNull(receivedMessage, "No message received in time");
        assertEquals("Hello Server via UDP", receivedMessage.getPayload());
    }

    // Test implementation of the Protocol
    static class TestProtocol implements Protocol {
        private final BlockingQueue<Message<?>> messages = new ArrayBlockingQueue<>(10);

        @Override
        public void receiveMessage(Server server, Message<?> message) {
            messages.add(message);
        }

        @Override
        public void onClientConnect(Server server, UUID clientId) {
        }

        @Override
        public void onClientDisconnect(Server server, UUID clientId) {
        }

        public Message<?> getMessage(long timeout, TimeUnit unit) throws InterruptedException {
            return messages.poll(timeout, unit);
        }
    }
}
