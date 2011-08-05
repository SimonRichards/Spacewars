package common;

import client.Client;
import java.awt.Dimension;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Random;
import java.util.Timer;
import server.Server;

/**
 *
 * @author Simon
 */
public class Game {

    private Game() {
    }

    public static final long GAME_PERIOD = 50;
    public static final int MAX_COMMANDS = 10;
    public static final Dimension appSize = new Dimension(500, 500);
    public static final int BROADCAST_PERIOD = 1000;
    public static final int DEFAULT_UDP_PORT = 9876;
    public static final int UDP_PACKET_LENGTH = 20;
    public static final int MAX_SERVER_CONNECTIONS = 50;
    public static final int MAX_SERVERS = 10;
    public static final int popcap = 100;
    public static final String group = "227.0.113.0";
    public static final int MIN_PORT = 1024;
    public static final int MAX_PORT = 65536;

    /**
     * Program entry point creates a new client and server.
     * Pass any argument or set env var HEADLESS to anything to
     * go into headless (no client) mode.
     * @param args
     */
    public static void main(String[] args) {
        int tcpPort = new Random().nextInt(MAX_PORT - MIN_PORT) + MIN_PORT;
        boolean headless = System.getenv().containsKey("HEADLESS") || args.length > 0;
        if (headless) System.out.println("headless mode");

        try {
            Server.start(tcpPort, headless);
            if (!headless) Client.start(tcpPort);

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
