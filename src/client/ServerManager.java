package client;

import common.Connection;
import common.Connection.Server;
import common.Game;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A ServerManager joins the multicast group and tracks currently available servers.
 * New servers are added to the clients list and old servers are refreshed so that
 * unresponsive ones may be cleared out. Servers are collected in a copy on write
 * array to allow fast asynchronous access to array elements and the occasional
 * array mutation (when servers start or stop broadcasting).
 * @author Simon, Daniel
 */
class ServerManager implements Runnable {

    private final MulticastSocket multiSocket;
    private final CopyOnWriteArrayList<Connection.Server> servers;
    private int serverSelector;
    private int current;
    private final int id;
    private ArrayList<String> names;

    /**
     * Connects to the local server and listens to
     * @param port
     * @param id
     * @throws IOException
     */
    ServerManager(int port, int id) throws IOException {
        servers = new CopyOnWriteArrayList<Server>();
        servers.add(new Server(InetAddress.getLocalHost(), port, "My server", id));
        multiSocket = new MulticastSocket(Game.DEFAULT_UDP_PORT);
        multiSocket.joinGroup(InetAddress.getByName(Game.group));
        this.id = id;
        names = new ArrayList<String>(Game.MAX_SERVERS);
        names.add("My server");
    }

    /**
     * Increments the users selection (of servers to hyperspace to). Skips the current
     * server and does nothing if there are no more valid servers in that direction.
     */
    void incrementSelector() {
        serverSelector = serverSelector == servers.size() - 1 ? servers.size() - 1 : serverSelector + 1;
        if (serverSelector == current) {
            if (current != servers.size() - 1) {
                serverSelector += 1;
            } else {
                serverSelector -= 1;
            }
        }
    }

    /**
     * Decrements the users selection (of servers to hyperspace to). Skips the current
     * server and does nothing if there are no more valid servers in that direction.
     */
    void decrementSelector() {
        serverSelector = serverSelector == 0 ? 0 : serverSelector - 1;
        if (serverSelector == current) {
            if (current != 0) {
                serverSelector -= 1;
            } else {
                serverSelector += 1;
            }
        }
    }

    /**
     * Removes the current server from the list and moves to another server
     */
    void removeCurrent() {
        names.remove(servers.get(current).getName());
        servers.remove(current);
        current = 0;
        hyper();
    }

    /**
     * @return Names of all collected servers
     */
    Collection<String> getNames() {
        return names;
    }

    /**
     * for use with the above method
     * @return The index of the current server
     */
    int getIndex() {
        return current;
    }

    /**
     * for use with the above methods
     * @return The index of the currently selected server
     */
    int getSelector() {
        return serverSelector;
    }

    /**
     * Only call this function once per loop, save the result
     * @return The server to use for one game loop
     */
    Server getCurrent() {
        while(servers.isEmpty());
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
     * @return True if a hyperspace jump is available, false otherwise
     * Please call before hyper()
     */
    boolean canHyper() {
        return current != serverSelector;
    }

    /**
     * Leaves the current server and joins the next selected one.
     * As the only connection reliant method that does not throw
     * its IOExceptions up the call chain, this method must take
     * care of server failure itself and repeatedly join servers
     * until one actually works.
     */
    void hyper() {
            servers.get(current).leave();
            int temp = current;
            current = serverSelector;
            serverSelector = temp;
            try {
                servers.get(current).join();
            } catch (IOException e) {
                // If this happens the client has attempted to
                // join a server that disconnected very recently
                current = 0;
                while (true) {
                    try {
                    servers.remove(current);
                    servers.get(0).join();
                    break;
                    } catch (IOException e2) {
                        // restart loop
                    }
                }
            }
    }

    /**
     * A continuous loop that finds new servers and validates current ones
     */
    @Override
    public void run() {
        try {
            byte[] buffer = new byte[Game.UDP_PACKET_LENGTH];
            DatagramPacket packet = new DatagramPacket(buffer, Game.UDP_PACKET_LENGTH);
            while (true) {
                boolean found = false;

                multiSocket.receive(packet);
                String[] data = new String(packet.getData()).split(" ");
                String name = data[1].trim().concat("'s server");
                int port = Integer.valueOf(data[0]);

                for (Server server : servers) {
                    if (server.is(packet.getAddress(), port)) {
                        server.heartbeat();
                        found = true;
                    }
                }

                if (!found) {
                    if (servers.size() < Game.MAX_SERVERS) {
                        servers.add(new Server(
                                packet.getAddress(),
                                port,
                                name,
                                id));
                    }
                    names.add(name);
                }
                for (int i = 0; i < buffer.length; i++) {
                    buffer[i] = 0;
                }

                if (current == serverSelector && servers.size() > 1) {
                    if (serverSelector > 0) {
                        serverSelector--;
                    } else if (serverSelector < servers.size()) {
                        serverSelector ++;
                    }
                }
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }

    /**
     * Starts this ServerManager object running, for convenience only
     */
    void start() {
        new Thread(this).start();
    }
}
