package client;

import common.Actor;
import common.Actor.ActorType;
import common.Command;
import common.Connection;
import common.Connection.Server;
import common.Game;
import java.io.IOException;
import java.net.InetAddress;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A client object bundles the user interface (display and input) along with
 * a ServerManager object to track all servers that the client is connected to.
 * @author Simon, Daniel
 */
public class Client implements Runnable {
    private final InputHandler input;
    private Display display;
    private Map<Integer, Actor> currentActors;
    private Map<Integer, Actor> nextActors;
    private double[] actorBuffer;
    private final int id;
    private int hyperCoolDown;
    private final int hyperPeriod = 5;
    private final ServerManager serverManager;

    /**
     * Starts up a client in its own thread, blocks until server is found
     * @param tcpPort The port on which to connect to the local server
     * @throws IOException If the local server cannot be found
     */
    public static void start(int tcpPort) throws IOException{
        new Thread(new Client(tcpPort)).start();
    }

    /**
     * Creates a new Client which blocks until the local
     * server is found.
     *
     * @param port The TCP/IP port to find the local server on
     * @throws IOException if there is an error in the TCP protocol
     */
    private Client(int port) throws IOException {
        id = new Random().nextInt();
        serverManager = new ServerManager(port, id);
        serverManager.start();
        input = new InputHandler();
        currentActors = new HashMap<Integer, Actor>(50);
        nextActors = new HashMap<Integer, Actor>(50);
        display = new Display(Game.appSize, input);
        actorBuffer = new double[Actor.NUM_ELEMENTS];
    }

    /**
     * The client side of the main game loop
     */
    @Override
    public void run() {
        while (true) {
            // Retrieve keyboard input
            EnumSet<Command> commands = input.read();

            // Exit at user's command
            if (commands.contains(Command.EXIT)) {
                System.exit(0);
                break;
            }

            // Retrieve the user's server selection and limit the value
            switch (input.getSelectionChange()) {
                case -1:
                    serverManager.decrementSelector();
                    break;
                case 1:
                    serverManager.incrementSelector();
                    break;
            }

            // Send ENTRY command iff user requests a respawn AND user is dead.
            if (commands.contains(Command.RESPAWN)) {
                if (!currentActors.containsKey(id)) {
                    commands.add(Command.ENTRY);
                }
                commands.remove(Command.RESPAWN);
            }


            // Handle hyperspace requests
            if (commands.contains(Command.HYPERSPACE)) {
                if (hyperCoolDown == 0 && serverManager.canHyper()) {
                    currentActors.clear();
                    hyperCoolDown = hyperPeriod;
                    serverManager.hyper();
                }
                // Do not send the hyperspace command to the server (ever)
                commands.remove(Command.HYPERSPACE);
            }
            if (hyperCoolDown > 0) {
                hyperCoolDown--;
            }

            // Retrieve the current server (also clears out dead servers)
            Server server = serverManager.getCurrent();

            // Send commands to server
            try {
                if (commands.size() > 4) {
                    System.out.println(commands.size());
                }
                server.send(commands);
                int numActors = server.receiveHeaders();
                for (int i = 0; i < numActors; i++) {
                    int id = server.receiveActor(actorBuffer, i);
                    if (currentActors.containsKey(id)) {
                        currentActors.get(id).updateFromStream(actorBuffer);
                        nextActors.put(id, currentActors.get(id));
                    } else {
                        ActorType type = server.getActorType(i);
                        nextActors.put(id, Actor.fromBuffer(type, id, actorBuffer));
                    }
                }
            } catch (IOException e) {
                serverManager.removeCurrent();
                continue;
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
                break;
            }

            // Push the actors into the display
            display.loadActors(nextActors.values());
            Map<Integer, Actor> temp = currentActors;

            // Flip the actor buffers
            currentActors = nextActors;
            nextActors = temp;
            nextActors.clear();

            // Send the list of servers to the display
            display.setServerNames(
                    serverManager.getNames(),
                    serverManager.getIndex(),
                    serverManager.getSelector());

            display.repaint();
        }
    }
}
