package common;

import common.Actor.ActorType;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Simon, Daniel
 */
public abstract class Connection {

    protected Socket socket;
    protected DataOutputStream out;
    protected DataInputStream in;

    public static class Client extends Connection {

        private int id;

        public Client(Socket socket) throws IOException {
            this.socket = socket;
            socket.setTcpNoDelay(true);
            out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            id = in.readInt();
        }

        public int getID() {
            return id;
        }

        /**
         *
         * @param commandBuffer
         * @return The number of commands received
         * @throws IOException
         */
        public int getCommands(int[] commandBuffer) throws IOException {
            int i = 0;
            while (in.available() > 0) {
                commandBuffer[i++] = in.readInt();
                if (i == Game.MAX_COMMANDS) {
                    System.err.println("too many commands received");
                    break;
                }
            }
            return i;
        }

        public void sendHeader(ArrayList<Actor> actors) throws IOException {
            out.writeInt(actors.size());
            for (Actor actor : actors) {
                out.writeInt(actor.getID());
                out.writeInt(actor.getActorType());
            }
            out.flush();
        }

        public void sendActor(double[] actorBuffer) throws IOException {
            for (int i = 0; i < actorBuffer.length; i++) {
                out.writeDouble(actorBuffer[i]);
            }
            out.flush();
        }

        public void close() {
            try {
                socket.close();
            } catch (IOException e) {
                //Don't care
            }
        }
    }

    public static class Server extends Connection {

        private final ArrayList<Integer> actorList;
        private final String name;
        private long lastRefreshed;
        private static final long TIMEOUT = 1200;

        public Server(InetAddress host, int port, String name, int id) throws IOException {
            this.name = name;
            actorList = new ArrayList<Integer>(Game.popcap);
            socket = new Socket(host, port);
            out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            out.writeInt(id);
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
            //TODO: port is used as an ad-hoc server unique identifier as different techniques failed on different netrworks
            if (socket.getPort() == port) {
                return true;
            } else {
                return false;
            }
            /*
             * Here's the old code:
             * socket.getLocalAddress().getHostAddress().equals(address.getHostAddress()) ||
             * (address.getHostAddress().equals(socket.getInetAddress().getHostAddress())
             */
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
         * @return The server's reported name
         */
        public String getName() {
            return name;
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
         * @return Number of actors n in the above comment
         * @throws IOException if the server is down
         */
        public int receiveHeaders() throws IOException {
            actorList.clear();
            int numActors = in.readInt();
            for (int i = 0; i < numActors * 2; i++) {
                actorList.add(in.readInt());
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

        public boolean isAlive() {
            return System.currentTimeMillis() - lastRefreshed < TIMEOUT;
        }

        public void heartbeat() {
            lastRefreshed = System.currentTimeMillis();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        socket.close();
    }


}
