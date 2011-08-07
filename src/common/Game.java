package common;

import client.Client;
import java.awt.Dimension;
import java.io.IOException;
import java.util.Random;
import server.Server;

/**
 *
 * @author Simon
 */
public final class Game {

    private Game() {}

    public static final long GAME_PERIOD = 50;
    public static final int COMMAND_BUFFER_SIZE = 10;
    public static final Dimension APPSIZE = new Dimension(500, 500);
    public static final int BROADCAST_PERIOD = 1000;
    public static final int DEFAULT_UDP_PORT = 9876;
    public static final int UDP_PACKET_LENGTH = 20;
    public static final int MAX_SERVER_CONNECTIONS = 50;
    public static final int MAX_SERVERS = 10;
    public static final int POPCAP = 100;
    public static final String MULTICAST_GROUP = "227.0.113.0";
    public static final int MIN_PORT = 1024;
    public static final int MAX_PORT = 65536;
    public static final Random rand = new Random(System.currentTimeMillis());

    /**
     * Program entry point creates a new client and server.
     * Pass any argument or set env var HEADLESS to anything to
     * go into headless (no client) mode.
     * @param args
     */
    public static void main(String[] args) {
        int tcpPort = new Random().nextInt(MAX_PORT - MIN_PORT) + MIN_PORT;
        boolean headless = System.getenv().containsKey("HEADLESS") || args.length > 0;

        try {
            Server.start(tcpPort, headless);
            if (!headless) new Client(tcpPort).start();

        } catch (IOException e) {
            System.err.println("No network connection found");
            System.exit(-1);
        }
    }
}
