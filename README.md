# socket-sandbox
## Concept
This is a library intended to be used as a replacement for Java's default socket library, at least for cases in which a client-server architecture is needed.
The implementation uses TCP to form a connection between clients and a server with a many to one relationship.
If several servers are needed for one application, you need to create separate clients for each server.
## Implementing
Add this library as a dependency to your project, and create a structure with the following objects:
### A Client Protocol
Create a new object implementing the interface Protocol<Client> and override the necessary interface functions. Each of these functions act as events, and are called whenever something happens.
For example, the receiveMessage function is called whenever the client socket receives a message.
The onClientConnect and onClientDisconnect functions are called when the client itself connects or disconnects.
### A Server Protocol
Create a new object implementing the interface Protocol<Server> and override the necessary interface functions.
Functions close to how the client protocol does, but onClientDisconnect and onClientConnect are called whenever *any* client connects or disconnects. The parameter is that client's session id.
### Client
The client object is resonsible for handling a client socket. It must have an address to connect to (the address of the server) and a port (the port of the server).
The constructor requires a protocol to be provided, which should be the client protocol you implemented earlier.
### Server
The server object is responsible for handling the server socket. It must have a port to open for connections.
The constructor requires a protocol to be provided, which should be the server protocol you implemented earlier.
### Message
The Message object is what will be sent between sockets. You *can* use the pure Message class;
However, it is recommended to extend the Message object and specifying a payload type (e.g. TextMessage<String> extends Message).
This is because the protocols' receiveMessage functions will always provide a wildcard-typed message (i.e. Message<?>), and specifying custom message types makes it easy to distinguish messages from each
other and determine the type of the payload.
## Usage
Once the library is implemented, you can connect the protocols to the rest of your application. Messages are received through the protocols, and messages can be sent through the socket, either the Client object or the Server object.
## Warnings
Do not create the server and the client on the same thread. This is because Java will not make the distinction between the server and the client, and as such treat the input and output streams for each
object as being for the same socket, resulting in many "Invalid Type Code" exceptions.
