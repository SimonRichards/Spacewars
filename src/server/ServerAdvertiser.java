package server;

import common.Game;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A server advertiser periodically sends Datagrams to the Multicast MULTICAST_GROUP defined in Game.
 * These packet contain the host's user name (for identification) and the TCP port which
 * the server is bound to. The client may determine the host's address by inspecting the packet.
 * @author Simon, Daniel
 */
class ServerAdvertiser extends TimerTask {

    private DatagramSocket socket = null;
    private DatagramPacket packet = null;

    /**
     * Creates the message to send and instantiates the Socket and Packet objects
     * @param tcpPort The port which the TCP based server is bound to
     * @throws IOException If the socket or packet fail to find the host
     */
    ServerAdvertiser(final int tcpPort) {
        super();
        final String name = tcpPort + " " + System.getProperty("user.name");
        int length = name.length() > Game.UDP_PACKET_LENGTH ? Game.UDP_PACKET_LENGTH : name.length();
        byte[] buffer = name.substring(0, length).getBytes();
        try {
            socket = new DatagramSocket();
            packet = new DatagramPacket(buffer, length, InetAddress.getByName(Game.MULTICAST_GROUP), Game.DEFAULT_UDP_PORT);
        } catch (IOException e) {
            System.err.println("Could not start UDP service");
            System.exit(-1);
        }

        // Start the service
        new Timer("Broadcaster", true).scheduleAtFixedRate(this, 0, Game.BROADCAST_PERIOD);
    }

    /**
     * Sends a single Datagram packet. On failure outputs to stderr. Not for external access.
     */
    @Override
    public void run() {
        try {
            socket.send(packet);
        } catch (IOException e) {
            System.err.println(e.getMessage() + "\nMulticast failed");
        }
    }
}
