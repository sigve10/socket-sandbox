package no.ntnu.sigve.sockets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import no.ntnu.sigve.communication.Message;

public class UdpClientSocket implements ClientSocket {

    private final String serverAddress;
    private final int serverPort;
    private DatagramSocket socket;
    private InetAddress inetAddress;

    public UdpClientSocket(String serverAddress, int serverPort) {
        if (serverPort <= 0 || serverPort > 65535) {
            throw new IllegalArgumentException("Invalid port number");
        }
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    @Override
    public void connect() throws IOException {
        this.socket = new DatagramSocket();
        this.inetAddress = InetAddress.getByName(serverAddress);
    }

    @Override
    public void sendMessage(Message<?> message) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(message);
        byte[] buffer = baos.toByteArray();

        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, inetAddress, serverPort);
        System.out.println("Sending message via UDP: " + message);
        socket.send(packet);
    }

    @Override
    public Message<?> receiveMessage() throws IOException, ClassNotFoundException {
        byte[] buffer = new byte[65535];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
    
        while (!socket.isClosed()) {
            try {
                System.out.println("Waiting to receive UDP message...");
                socket.setSoTimeout(10000);
                socket.receive(packet);
    
                ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData(), packet.getOffset(), packet.getLength());
                ObjectInputStream ois = new ObjectInputStream(bais);
                return (Message<?>) ois.readObject();
            } catch (SocketTimeoutException e) {
                System.out.println("UDP receive timed out, retrying...");
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error receiving UDP message: " + e.getMessage());
                throw e;
            }
        }
        return null;
    }
    

    @Override
    public void close() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

        @Override
    public boolean isClosed() {
        return socket == null || socket.isClosed();
    }

    public int getServerPort() {
        return this.serverPort;
    }

}
