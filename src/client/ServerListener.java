package client;

import common.Connection;
import common.Game;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Simon, Daniel
 */
public class ServerListener implements Runnable{

    private final Client client;
    public ServerListener(Client client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[10];
            DatagramPacket packet = new DatagramPacket(buffer, 10);
            DatagramSocket socket = new DatagramSocket(Game.DEFAULT_UDP_PORT);
            while (Game.running) {
                socket.receive(packet);
                if (client.numServers() < Game.MAX_SERVERS &&
                    !client.serverAlreadyConnected(packet.getAddress())) {
                        client.addServer(new Connection.Server(
                        packet.getAddress(),
                        Game.DEFAULT_TCP_PORT,
                        new String(buffer).trim().concat("'s server")));
                }
                for (int i = 0; i < buffer.length; i++) {
                    buffer[i] = 0;
                }
            }
        } catch (IOException ex) {
            Game.running = false;
            System.err.println("Server listener failed");
        }
    }
}
