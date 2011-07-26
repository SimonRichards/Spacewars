package client;

import common.Connection;
import common.Game;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 *
 * @author Simon, Daniel
 */
public class ServerListener implements Runnable{

    private final Client client;
    private final MulticastSocket multiSocket;

    public ServerListener(Client client) throws IOException {
        this.client = client;
        multiSocket = new MulticastSocket(Game.DEFAULT_UDP_PORT);
        multiSocket.joinGroup(InetAddress.getByName(Game.group));
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[Game.UDP_PACKET_LENGTH];
            DatagramPacket packet = new DatagramPacket(buffer, Game.UDP_PACKET_LENGTH);
            while (true) {
                multiSocket.receive(packet);
                    String []data = new String(packet.getData()).split(" ");
                    String name = data[1].trim().concat("'s server");
                    int port = Integer.valueOf(data[0]);
                if (client.numServers() < Game.MAX_SERVERS && // true){
                    !client.serverAlreadyConnected(packet.getAddress(), port)) {

                    client.addServer(new Connection.Server(
                        packet.getAddress(),
                        port,
                        name));
                }
                for (int i = 0; i < buffer.length; i++) {
                    buffer[i] = 0;
                }
            }
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            System.exit(-20);
        }
    }
}
