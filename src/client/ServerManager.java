package client;

import common.Connection;
import common.Connection.Server;
import common.Game;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A ServerManager joins the multicast group and tracks currently available servers.
 * New servers are added to the clients list and old servers are refreshed so that
 * unresponsive ones may be cleared out. Servers are collected in a copy on write
 * array to allow fast asynchronous access to array elements and the occasional
 * array mutation (when servers start or stop broadcasting).
 * @author Simon, Daniel
 */
class ServerManager extends Thread {

    private static final String LOCAL_SERVER_NAME = "Local";
    private final MulticastSocket multiSocket;
    private final List<Connection.Server> servers;
    private int current;
    private final int clientID;
    private final Collection<String> names;
    private final byte[] buffer;
    private final DatagramPacket packet;
    private final Map<String, Integer> serverCounter;

    /**
     * Connects to the local server joins the multicast group
     * @param port The local server's TCP port
     * @param clientID The client's ID
     * @throws IOException if the UDP or TCP connections fail
     */
    ServerManager(int port, int clientID) throws IOException {
        super("ServerManager");
        serverCounter = new HashMap<String, Integer>(Game.MAX_SERVERS, 1.0f);
        String name = getServerName(LOCAL_SERVER_NAME);
        servers = new CopyOnWriteArrayList<Server>(); // Mutations are rare, access isn't
        servers.add(new Server(InetAddress.getLocalHost(), port, name, clientID));
        multiSocket = new MulticastSocket(Game.DEFAULT_UDP_PORT);
        multiSocket.joinGroup(InetAddress.getByName(Game.MULTICAST_GROUP));
        this.clientID = clientID;
        names = new ArrayList<String>(Game.MAX_SERVERS);
        names.add(name);
        buffer = new byte[Game.UDP_PACKET_LENGTH];
        packet = new DatagramPacket(buffer, Game.UDP_PACKET_LENGTH);
    }

    /**
     * Removes the current server from the list and moves to another server
     */
    void removeCurrent() {
        boolean found = false;
        while (!found) {
            try {
                names.remove(servers.get(current).getName());
                servers.remove(current);
                current = 0;
                servers.get(current).join();
                found = true;
            } catch (IOException e) {
                continue;
            }
        }
    }

    /**
     * @return Names of all collected servers
     */
    Collection<String> getNames() {
        return Collections.unmodifiableCollection(names);
    }

    /**
     * for use with the above method
     * @return The index of the current server
     */
    int getCurrentIndex() {
        return current;
    }

    /**
     * Only call this function once per loop, save the result
     * @return The server to use for one game loop
     */
    Server getCurrent() {
        if (servers.isEmpty()) {
            System.err.println("All server connections lost");
            System.exit(-1);
        }

        for (Connection.Server server : servers) {
            if (!server.isAlive()) {
                servers.remove(server);
                names.remove(server.getName());
            }
        }
        if (current >= servers.size()) {
            current = servers.size() - 1;
        }
        return servers.get(current);
    }

    /**
     * Leaves the current server and joins the next selected one.
     * As the only connection reliant method that does not throw
     * its IOExceptions up the call chain, this method must take
     * care of server failure itself and repeatedly join servers
     * until one actually works.
     */
    boolean hyper() {
        if (servers.size() <= 1) {
            return false;
        } else {
            servers.get(current).leave();
            int temp = current;
            do {
                current = Game.rand.nextInt(servers.size());
            } while (current == temp);

            try {
                servers.get(current).join();
            } catch (IOException e) {
                System.err.println("Couldn't hyper to new server");
                // If this happens the client has attempted to
                // join a server that disconnected very recently
                current = 0;
                while (true) {
                    try {
                        servers.remove(current);
                        servers.get(0).join();
                        break;
                    } catch (IOException e2) {
                        System.err.println("failed to join server");
                        continue;
                    }
                }
            }
            return true;
        }
    }

    /**
     * A continuous loop that finds new servers and validates current ones
     */
    @Override
    public void run() {
        int port;
        boolean found;
        String name;
        String[] data;
        try {
            while (true) {
                found = false;

                // Block until a datagram is received
                multiSocket.receive(packet);

                // Decode the datagram
                data = new String(packet.getData()).split(" ");
                port = Integer.valueOf(data[0]);

                // Search for matching server and refresh its timeout counter
                // if found
                for (Server server : servers) {
                    if (server.is(packet.getAddress(), port)) {
                        server.heartbeat();
                        found = true;
                        break;
                    }
                }

                // If it's a new server then add it to the pool
                if (!found) {
                    name = getServerName(
                            packet.getAddress().getHostAddress().equals(
                            InetAddress.getLocalHost().getHostAddress()) ?
                            LOCAL_SERVER_NAME :
                            data[1].trim() + "'s");
                    if (servers.size() < Game.MAX_SERVERS) {
                        servers.add(new Server(
                                packet.getAddress(),
                                port,
                                name,
                                clientID));
                    }
                    names.add(name);
                }

                // Clear the buffer
                for (int i = 0; i < buffer.length; i++) {
                    buffer[i] = 0;

                }
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }


    /**
     * @param address The server's address
     * @return A unique name for the server
     */
    private String getServerName(String name) {
        if (serverCounter.containsKey(name)) {
            serverCounter.put(name, serverCounter.get(name) + 1);
        } else {
            serverCounter.put(name, 1);
        }
        return name + " server " + serverCounter.get(name);
    }
}
