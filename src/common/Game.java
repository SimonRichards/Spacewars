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

    public static final Dimension appSize = new Dimension(500, 500);
    public static final int BROADCAST_PERIOD = 1000;
    public static final int DEFAULT_UDP_PORT = 9876;
    public static final int UDP_PACKET_LENGTH = 20;
    public static final int MAX_SERVER_CONNECTIONS = 50;
    public static final int MAX_SERVERS = 10;
    public static final String group = "227.0.113.0";

    public static final int MIN_PORT = 1024;
    public static final int MAX_PORT = 65536;

    /**
     * Program entry point
     * @param args
     */
    public static void main(String[] args) {
        Random rand = new Random();
        int tcpPort = rand.nextInt(MAX_PORT - MIN_PORT) + MIN_PORT;
        Thread serverThread, clientThread;
        if (System.getenv().containsKey("HEADLESS")) {
            System.out.println("headless mode");
            try {
//                serverThread = new Thread(new Server(tcpPort, true), Server.class.getName());
                Server server = new Server(tcpPort, true);
                new Timer().scheduleAtFixedRate(server, 0, 50);
//                serverThread.start();
//                serverThread.join();
            } catch (IOException ex) {
                System.out.println("headless mode failed");
            } //catch (InterruptedException ex) {
//                System.out.println("headless mode failed");
//            }
        } else {
            try {
                Server server = new Server(tcpPort, false);
                new Timer().scheduleAtFixedRate(server, 0, 50);
                clientThread = new Thread(new Client(tcpPort), Client.class.getName());
                clientThread.start();
                clientThread.join();

            } catch (IOException e) {
                System.err.println(e.getMessage() + "\nError on bind");
                System.exit(-1);
            } catch (InterruptedException e) {
                System.err.println(e.getMessage() + "\nThread interrupted");
                System.exit(-1);
            }
        }
        System.exit(0);
    }
}
