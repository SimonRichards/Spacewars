package server;

import static java.net.InetAddress.getByName;
import common.Game;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Simon, Daniel
 */
public class UDPBroadcaster extends TimerTask {

    private int tcpPort;
    private byte[] buffer;
    private int length;
    private DatagramSocket socket;
    private DatagramPacket packet;

    private static boolean running;

    UDPBroadcaster(int tcpPort) throws IOException {
        Random rand = new Random();
        this.tcpPort= tcpPort;
        String name = String.valueOf(tcpPort) + " " + System.getProperty("user.name");
        length = name.length() > Game.UDP_PACKET_LENGTH ? Game.UDP_PACKET_LENGTH : name.length();
        buffer = name.substring(0, length).getBytes();

        socket = new DatagramSocket();

        packet = new DatagramPacket(buffer, length, InetAddress.getByName(Game.group), Game.DEFAULT_UDP_PORT);
    }

    @Override
    public void run() {
        try {
            socket.send(packet);
        } catch (IOException e) {
            System.err.println(e.getMessage() + "\nBroadcast failed");
            System.exit(-23);
        }
    }

    public static void start(int tcpPort) { //Enforces singleton
        if (!running) {
            try {
                new Timer("Broadcaster", true).scheduleAtFixedRate(new UDPBroadcaster(tcpPort), 0, Game.BROADCAST_PERIOD);
                running = true;
            } catch (IOException ex) {
                System.err.println("Could not start broadcast service");
                System.exit(-3);
            }
        }
    }
}
