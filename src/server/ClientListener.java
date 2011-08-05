package server;

import common.Connection;
import common.Connection.Client;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.LinkedList;

/**
 *
 * ClientListener objects block in their own thread until a client connects.
 * These clients may then be retrieved by another thread at a time of its choosing.
 * @author Simon, Daniel
 */
class ClientListener extends Thread {

    private final ServerSocket socket;
    private final LinkedList<Connection.Client> clientBuffer;

    /**
     * Creates a new ClientListener on the given port.
     * @param port The port to bind to.
     * @throws IOException if the ServerSocket cannot bind to the given port
     */
    ClientListener(int port) throws IOException{
        socket = new ServerSocket(port);
        clientBuffer = new LinkedList<Connection.Client>();
    }

    /**
     * Thread entry point
     */
    @Override
    public void run() {
        while (true) {
            Socket connection;
            try {
                connection = socket.accept();
                synchronized (this) {
                    clientBuffer.add(new Connection.Client(connection));
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }

    /**
     * Waits for a client to connect and returns when one does.
     * Will wait indefinitely if none connect.
     * @return
     */
    Connection.Client blockUntilClient() {
        Socket clientSocket;
        try {
            clientSocket = socket.accept();
            return new Connection.Client(clientSocket);
        } catch (IOException e) {
            System.err.println("local client failed to connect");
            System.exit(-1);
            return null;
        }
    }

    /**
     * Loads all waiting clients into the given collection
     * @param clients The client collection to add to
     */
    void loadNewClients(Collection<Client> clients) {
        clients.addAll(clientBuffer);
        clientBuffer.clear();
    }
}
