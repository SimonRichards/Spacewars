package common;

import common.Actor.ActorType;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Sub-classes of Connection abstract sending and receiving data to
 * their respective endpoints, all important operations throw any
 * IO Exceptions they cause so it is up to the user to handle these.
 * @author Simon, Daniel
 */
public abstract class Connection {

    protected Socket socket;
    protected DataOutputStream out;
    protected DataInputStream in;
    protected String name;

    /**
     * @return The other end's reported name
     */
    public String getName() {
        return name;
    }

    /**
     * Connection.Client encapsulates the server's communication with a client
     */
    public static class Client extends Connection {

        private static final int MAX_NAME_LENGTH = 20;
        private final int id;

        /**
         * Creates a new client connection on the given socket
         * @param socket The socket on which a client is already connected
         * @throws IOException If the handshake data is not received correctly
         */
        public Client(Socket socket) throws IOException {
            this.socket = socket;
            socket.setTcpNoDelay(true);
            out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            id = in.readInt();

            char[] buffer = new char[MAX_NAME_LENGTH];
            int i = 0;
            do {
                buffer[i] = in.readChar();
            } while (buffer[i] != '\n' && ++i < MAX_NAME_LENGTH);
            name = String.copyValueOf(buffer);
        }

        /**
         * @return The client's unique identifier
         */
        public int getID() {
            return id;
        }

        /**
         * Fills a command buffer with the users input commands.
         * @param commandBuffer an empty buffer to be filled with the ordinal
         * values of the client's commands
         * @return The number of commands received
         * @throws IOException If a connection problem occurs
         */
        public int getCommands(int[] commandBuffer) throws IOException {
            int i = 0;
            while (in.available() > 0) {
                commandBuffer[i++] = in.readInt();
                if (i == Game.COMMAND_BUFFER_SIZE) {
                    System.err.println("too many commands received");
                    break;
                }
            }
            return i;
        }

        /**
         * Sends the client a list of identifiers for all current actors on this server
         * @param actors The actors to convert into a header and transmit
         * @param clients The clients currently connected to the server (their names are sent)
         * @throws IOException if transmission fails
         */
        public void sendHeader(Collection<Actor> actors, Collection<Client> clients) throws IOException {
            out.writeInt(actors.size());
            for (Actor actor : actors) {
                out.writeInt(actor.getID());
                out.writeInt(actor.getActorType());
            }

            out.writeShort(clients.size());
            for (Client client : clients) {
                out.writeUTF(client.name);
            }
            out.flush();
        }

        /**
         * Takes an already marshaled actor and transmits
         * @param actorBuffer The actor data to transmit
         * @throws IOException if transmission fails
         */
        public void sendActor(double[] actorBuffer) throws IOException {
            for (int i = 0; i < actorBuffer.length; i++) {
                out.writeDouble(actorBuffer[i]);
            }
            out.flush();
        }

        /**
         * Attempts to close the socket, failure not registered
         */
        public void close() {
            try {
                socket.close();
            } catch (IOException e) {
                //Don't care
            }
        }
    }

    /**
     * Encapsulates how a client sees a server and handles all data
     * transmission and reception.
     */
    public static class Server extends Connection {

        private final List<Integer> actorList;
        private long lastRefreshed;
        private static final long TIMEOUT = 1200;

        /**
         * Attempts a connection to the given machine and save's its name and id
         * @param host The server's IP address
         * @param port The port the server is listening on
         * @param name The server's name
         * @param id The client's id
         * @throws IOException If the connection cannot be made
         */
        public Server(InetAddress host, int port, String name, int id) throws IOException {
            this.name = name;
            actorList = new ArrayList<Integer>(Game.POPCAP);
            socket = new Socket(host, port);
            out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            out.writeInt(id);
            out.writeChars(System.getProperty("user.name") + '\n');
            socket.setSoTimeout(1000);
            socket.setTcpNoDelay(true);
            lastRefreshed = System.currentTimeMillis();
            out.flush();
        }

        /**
         * Checks if this is the same as a potential connection
         * @param address The other server's address
         * @param port The other server's port
         * @return true if this is the same server, false otherwise
         */
        public boolean is(InetAddress address, int port) {
            //TODO: port is used as an ad-hoc server unique identifier as other
            // techniques failed on different networks.
            return (socket.getPort() == port);
        }

        /**
         * Sends a group of commands by their ordinal values
         * @param commands The command set to send
         * @throws IOException if the link to the server was lost
         */
        public void send(Collection<Command> commands) throws IOException {
            for (Command command : commands) {
                out.writeInt(command.ordinal());
            }
            out.flush();
        }

        /**
         * Tells the server the the spacecraft is leaving, the client will stay connected
         */
        public void leave() {
            try {
                out.writeInt(Command.EXIT.ordinal());
                out.flush();
            } catch (IOException e) {
                // don't care
            }
        }

        /**
         * Tells the server that the client wishes to join the server's game
         * @throws IOException
         */
        public void join() throws IOException {
            out.writeInt(Command.ENTRY.ordinal());
            out.flush();
        }

        /**
         * Retrieves the series of n + 1 ints that the server sends before actors
         * Retrieves a list of connected client's  names
         * @param clientNames A collection to fill with client names
         * @return Number of actors n in the above comment
         * @throws IOException if the server is down
         */
        public int receiveHeaders(Collection<String> clientNames) throws IOException {
            actorList.clear();
            int numActors = in.readInt();
            for (int i = 0; i < numActors * 2; i++) {
                actorList.add(in.readInt());
            }

            short numClients = in.readShort();
            for (int i = 0; i < numClients; i++) {
                clientNames.add(in.readUTF().trim());
            }

            return numActors;
        }

        /**
         * Fills a buffer up with the values needed to update or recreate an actor
         * @param actorBuffer
         * @param index The actor's index
         * @return The actor's id
         * @throws IOException
         */
        public int receiveActor(double[] actorBuffer, int index) throws IOException {
            for (int i = 0; i < actorBuffer.length; i++) {
                actorBuffer[i] = in.readDouble();
            }
            return actorList.get(2 * index);
        }

        /**
         *
         * @param i
         * @return the ordinal value of the actor from
         */
        public ActorType getActorType(int i) {
            return Actor.ActorType.fromInt(actorList.get(2 * i + 1));
        }

        /**
         * Tests that a heartbeat has been received within the timeout period
         * @return true if the server is still sending UDP packets
         */
        public boolean isAlive() {
            return System.currentTimeMillis() - lastRefreshed < TIMEOUT;
        }

        /**
         * Notifies this object that a heartbeat was received from its remote host
         */
        public void heartbeat() {
            lastRefreshed = System.currentTimeMillis();
        }
    }
}
