package server;

import static java.net.InetAddress.getByName;
import common.Game;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Simon, Daniel
 */
public class UDPBroadcaster extends TimerTask {

    private int port;
    private byte[] buffer;
    private int length;
    private DatagramSocket socket;
    private DatagramPacket packet;

    private static boolean running;

    UDPBroadcaster() throws IOException {
        this.port = Game.DEFAULT_UDP_PORT;
        String name = System.getProperty("user.name");
        length = name.length() > Game.UDP_PACKET_LENGTH ? Game.UDP_PACKET_LENGTH : name.length();
        buffer = name.substring(0, length).getBytes();
        socket = new DatagramSocket(port, InetAddress.getLocalHost());
        packet = new DatagramPacket(buffer, length, getByName("192.168.1.255"), port);
    }

    @Override
    public void run() {
        try {
            socket.send(packet);
        } catch (IOException e) {
            System.err.println(e.getMessage() + "\nBroadcast failed");
        }
    }

    public static void start() { //Enforces singleton as only one port may be claimed per machine (atm)
        if (!running) {
            try {
                new Timer("Broadcaster", true).scheduleAtFixedRate(new UDPBroadcaster(), 0, Game.BROADCAST_PERIOD);
                running = true;
            } catch (IOException ex) {
                System.err.println("Could not start broadcast service");
                Game.running = false;
            }
        }
    }
}
