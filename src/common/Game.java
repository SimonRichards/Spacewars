package common;

import client.Client;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.io.IOException;
import java.sql.Time;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import server.Server;

/**
 *
 * @author Simon
 */
public class Game {

    private Game() {
    }
    public static boolean running = true;
    public static final int BROADCAST_PERIOD = 1000;
    public static final Dimension appSize = new Dimension(500, 500);
    public static final int DEFAULT_UDP_PORT = 60283;
    public static final int DEFAULT_TCP_PORT = 41252;
    public static final int UDP_PACKET_LENGTH = 10;
    public static final int MAX_SERVER_CONNECTIONS = 50;
    public static final int MAX_SERVERS = 10;

    /**
     * Program entry point
     * @param args
     */
    public static void main(String[] args) {
        Thread serverThread, clientThread;
        if (System.getenv().containsKey("HEADLESS")) {
            System.out.println("headless mode");
            try {
                serverThread = new Thread(new Server(DEFAULT_TCP_PORT, true), Server.class.getName());
                serverThread.start();
                serverThread.join();
            } catch (IOException ex) {
                System.out.println("headless mode failed");
            } catch (InterruptedException ex) {
                System.out.println("headless mode failed");
            }
        } else {
            try {
                Server server = new Server(DEFAULT_TCP_PORT, false);
                new Timer().scheduleAtFixedRate(server, 0, 50);
                clientThread = new Thread(new Client(DEFAULT_TCP_PORT), Client.class.getName());
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
