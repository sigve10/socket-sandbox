package com.example.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A client connection to a server. Capable of continuously reading information from the client and
 * sending messages.
 *
 * @see Client#sendOutgoingMessage(String) sendOutgoingMessage
 * @see Client#nextIncomingMessage() nextIncomingMessage
 *
 * @author Sigve Bjørkedal
 */
public class Client {

  private LinkedList<String> incomingMessages;
  private PrintWriter output;
  private Socket socket;
  private List<MessageObserver> observers;

  /**
   * Creates a new client connection to a server.
   *
   * @param address the address of the server to connect to
   * @param port the port of the server to connect to
   * @throws IOException if connecting to the server fails
   */
  public Client(String address, int port) throws IOException {
    this.incomingMessages = new LinkedList<>();
    this.socket = new Socket(address, port);
    this.observers = new ArrayList<>();

    BufferedReader socketResponseStream = new BufferedReader(
      new InputStreamReader(this.socket.getInputStream())
    );

    this.output = new PrintWriter(this.socket.getOutputStream(), true);

    new ClientListener(this, socketResponseStream).start();
  }

  /**
   * Sends a message to the server.
   *
   * @param message message to send to the server.
   */
  public void sendOutgoingMessage(String message) {
    this.output.println(message);
  }

  /**
   * Attempts to retrieve the earliest received message from the server.
   *
   * @return the earliest received message, or null if it does not exist.
   */
  public String nextIncomingMessage() {
    String retval = null;

    if (this.incomingMessages.peek() != null) {
      retval = this.incomingMessages.getFirst();
      this.incomingMessages.removeFirst();
    }

    return retval;
  }

  /**
   * Registers a new message to the client. Can be read through {@link Client#nextIncomingMessage
   * nextIncomingMessage}.
   *
   * @param message the message to register.
   */
  public void registerIncomingMessage(String message) {
    this.incomingMessages.add(message);
    notifyObservers(message);
  }

  /**
   * Adds an observer to the client. The observer will be notified of new messages.
   *
   * @param observer The observer to add.
   */
  public void addObserver(MessageObserver observer) {
    this.observers.add(observer);
  }

  /**
   * Removes an observer from the client. The observer will no longer receive message notifications.
   *
   * @param observer The observer to remove.
   */
  public void removeObserver(MessageObserver observer) {
    this.observers.remove(observer);
  }

  /**
   * Notifies all registered observers with the given message. This method is called
   * when a new message is received and needs to be communicated to all observers.
   *
   * @param message The message to be sent to the observers.
   */
  private void notifyObservers(String message) {
    for (MessageObserver observer : this.observers) {
      observer.update(message);
    }
  }

  /**
   * Notifies observers about the disconnection.
   */
  public void notifyDisconnection() {
    // Notify observers of disconnection
    for (MessageObserver observer : this.observers) {
      observer.update("Disconnected");
    }
    // Perform additional disconnection handling here if necessary
  }

  /**
   * Notifies observers about an exception.
   *
   * @param e The exception that occurred.
   */
  public void notifyException(Exception e) {
    for (MessageObserver observer : this.observers) {
      observer.update("Exception occurred: " + e.getMessage());
    }
  }
}
