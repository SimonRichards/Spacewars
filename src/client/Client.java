package client;

import common.Actor;
import common.Command;
import common.Connection;
import common.Connection.Server;
import common.Game;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Random;

/**
 *
 * @author Simon, Daniel
 */
public class Client implements Runnable {

    private Connection.Server currentServer;
    private LinkedList<Connection.Server> servers;
    private final InputHandler input;
    private Display display;
    private Collection<Actor> actors;
    private final Random rand;

    public Client(int port) throws IOException {
        servers = new LinkedList<Connection.Server>();
        currentServer = new Connection.Server(InetAddress.getLocalHost(), port, "Local server");
        servers.add(currentServer);
        input = new InputHandler();
        actors = new LinkedList<Actor>();
        rand = new Random();
        display = new Display(Game.appSize, input);
    }

    @Override
    public void run() {
        new Thread(new ServerListener(this)).start();

        while (Game.running) {
            // Retrieve keyboard input
            EnumSet<Command> commands = input.read();

            // Check for hyperspace
            if (commands.contains(Command.HYPERSPACE)) {
                currentServer.writeln(Command.EXIT.ordinal());
                currentServer = findNewServer();
                currentServer.writeln(Command.ENTRY.ordinal());
                continue;
            }

            // Exit at user's command
            if (commands.contains(Command.EXIT)) {
                Game.running = false;
                currentServer.writeln(Command.EXIT.ordinal());
                break;
            }


            // Send commands to server
            for (Command command : commands) {
                currentServer.writeln(command.ordinal());
            }

            String incoming = "";
            // Receive all actor states from server
            try {
                int numActors = currentServer.getNumActors();
                for (int i = 0; i < numActors; i++) {
                    incoming = currentServer.readln();
                    actors.add(Actor.fromStream(incoming));
                }
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
                removeServer(currentServer);
                currentServer = findNewServer();
                continue;
            } catch (Exception e) {
                System.err.println(e.getMessage());
                Game.running = false;
                break;
            }

            // Remove any disconnected servers
            for (Connection.Server server : servers) {
                if (!server.isAlive()) {
                    removeServer(server);
                }
            }

            // Push the actors into the display
            display.loadActors(actors);
            actors.clear();
            // Send the list of servers to the display
            display.setServerNames(servers);
            display.repaint();
        }

        for (Connection.Server server : servers) {
            server.close();
        }
    }

    private synchronized Connection.Server findNewServer() {//TODO: not synchronised?
        int current = servers.indexOf(currentServer);
        int nextInt = -1;
        do {
            try {
                nextInt = rand.nextInt(servers.size()); //TODO: threw an exceptoin, arg must be positive
            } catch (IllegalArgumentException e) {
                System.err.println("No servers left");
                System.exit(-1); //TODO move somewhere else
            }
        } while (current != nextInt);
        return servers.get(nextInt);
    }

    private synchronized void removeServer(Connection.Server server) {
        servers.remove(server);
    }

    public synchronized void addServer(Connection.Server server) {
        servers.add(server);
    }

    public synchronized boolean serverAlreadyConnected(InetAddress address) { //TODO: not synchronised?
        for (Server server : servers) {
            if (server.is(address)) {
                return true;
            }
        }
        return false;
    }

    public synchronized int numServers() {//TODO: not synchronised?
        return servers.size();
    }
}
