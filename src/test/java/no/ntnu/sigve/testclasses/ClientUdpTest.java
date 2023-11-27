package no.ntnu.sigve.testclasses;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.UUID;
import no.ntnu.sigve.client.Client;
import no.ntnu.sigve.communication.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class ClientUdpTest {
    private Client client;
    private DatagramSocket mockSocket;
    private final UUID dummySessionId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
    try {
            mockSocket = mock(DatagramSocket.class);
    
            client = new Client("localhost", 8080, 9090) {
                @Override
                public void connect() {
                    this.sessionId = dummySessionId;
                }
    
                @Override
                protected DatagramSocket createDatagramSocket() {
                    return mockSocket;
                }
    
                @Override
                public Message<?> receiveUdpMessage() {
                    Message<?> mockMessage = new Message<>(dummySessionId, "Received via UDP");
                    mockMessage.setSessionId(dummySessionId);
                    System.out.println("Mocked receiveUdpMessage: Session ID = " + mockMessage.getSessionId());
                    return mockMessage;
                }
            };
    
            client.connect();
        } catch (IOException e) {
            e.printStackTrace();
            fail("SetUp failed due to IOException: " + e.getMessage());
        }
    }
    
    @AfterEach
    void tearDown() {
    }

    @Test
    public void testSendUdpMessage() {
        assertNotNull(client, "Client is null, setUp method failed.");

        try {
            Message<String> message = new Message<>(dummySessionId, "Test UDP Message");
            client.sendUdpMessage(message);

            verify(mockSocket, times(1)).send(any(DatagramPacket.class));
        } catch (IOException e) {
            e.printStackTrace();
            fail("IOException during testSendUdpMessage: " + e.getMessage());
        }
    }

    @Test
    public void testReceiveUdpMessage() {
        assertNotNull(client, "Client is null, setUp method failed.");

        try {
            Message<?> message = client.receiveUdpMessage();
            assertEquals("Received via UDP", message.getPayload());
            assertEquals(dummySessionId, message.getSessionId());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            fail("Exception during testReceiveUdpMessage: " + e.getMessage());
        }
    }
}
