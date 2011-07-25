package server;

import common.Connection;
import common.Game;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Simon, Daniel
 */
public class ClientListener implements Runnable {

    private final Server server;
    private final ServerSocket socket;

    public ClientListener(Server server, ServerSocket socket) {
        this.server = server;
        this.socket = socket;
    }

    @Override
    public void run() {
        while (Game.running) {

            Socket connection;
            try {
                connection = socket.accept();
                server.addClient(new Connection.Client(connection));
                System.out.println("new client connected");
            } catch (IOException ex) {
                System.err.println("Server socket lost");
                Game.running = false;
            }
        }
    }

    public static void start(Server server, ServerSocket socket) {
        new Thread(new ClientListener(server, socket)).start();
    }
}
