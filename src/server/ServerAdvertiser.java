package server;

import common.Game;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A server advertiser periodically sends Datagrams to the Multicast group defined in Game.
 * These packet contain the host's user name (for identification) and the TCP port which
 * the server is bound to. The client may determine the host's address by inspecting the packet.
 * @author Simon, Daniel
 */
class ServerAdvertiser extends TimerTask {

    private byte[] buffer;
    private int length;
    private DatagramSocket socket;
    private DatagramPacket packet;

    /**
     * Creates the message to send and instantiates the Socket and Packet objects
     * @param tcpPort The port which the TCP based server is bound to
     * @throws IOException If the socket or packet fail to find the host
     */
    private ServerAdvertiser(int tcpPort) throws IOException {
        String name = String.valueOf(tcpPort) + " " + System.getProperty("user.name");
        length = name.length() > Game.UDP_PACKET_LENGTH ? Game.UDP_PACKET_LENGTH : name.length();
        buffer = name.substring(0, length).getBytes();
        socket = new DatagramSocket();
        packet = new DatagramPacket(buffer, length, InetAddress.getByName(Game.group), Game.DEFAULT_UDP_PORT);
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

    /**
     * Instantiates and schedules a ServerAdvertiser for periodic
     * @param tcpPort The port which the TCP based server is bound to
     */
    static void start(int tcpPort) {
        try {
            new Timer("Broadcaster", true).scheduleAtFixedRate(new ServerAdvertiser(tcpPort), 0, Game.BROADCAST_PERIOD);
        } catch (IOException e) {
            System.err.println("Could not start multicast service");
        }
    }
}
