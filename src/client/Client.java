package client;

import common.Actor;
import common.Actor.ActorType;
import common.Command;
import common.Connection.Server;
import common.Game;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

/**
 * A client object bundles the user interface (display and input) along with
 * a ServerManager object to track all servers that the client is connected to.
 * @author Simon, Daniel
 */
public class Client implements Runnable {

    private final InputHandler input;
    private final Display display;
    private Map<Integer, Actor> currentActors;
    private Map<Integer, Actor> nextActors;
    private final double[] actorBuffer;
    private int hyperCoolDown;
    private static final int HYPERPERIOD = 5;
    private final ServerManager serverManager;
    private final Collection<String> clientNames;
    private Server server;

    /**
     * Starts up a client in its own thread, blocks until server is found.
     * @param tcpPort The port on which to connect to the local server
     * @throws IOException If the local server cannot be found
     */
    public static void start(final int tcpPort) throws IOException {
        new Thread(new Client(tcpPort)).start();
    }

    /**
     * Creates a new Client which blocks until the local
     * server is found.
     *
     * @param port The TCP/IP port to find the local server on
     * @throws IOException if there is an error in the TCP protocol
     */
    Client(final int port) throws IOException {
        serverManager = new ServerManager(port, new Random().nextInt());
        serverManager.start();
        input = new InputHandler();
        currentActors = new HashMap<Integer, Actor>(50);
        nextActors = new HashMap<Integer, Actor>(50);
        display = new Display(Game.APPSIZE, input);
        actorBuffer = new double[Actor.NUM_ELEMENTS];
        clientNames = new LinkedList<String>();
    }

    /**
     * The client side of the main game loop
     */
    @Override
    public void run() {
        while (true) {
            try {
                server = serverManager.getCurrent();
                handleCommands(input.read());
                receiveState();
                updateDisplay();
            } catch (IOException e) {
                e.printStackTrace();
                serverManager.removeCurrent();
                continue;
            }
        }
    }

    /**
     * Sends commands onto the current server after giving special handling to
     * EXIT and HYPERSPACE
     * @param commands The current command set
     * @throws IOException if the server cannot be contacted
     */
    private void handleCommands(final EnumSet<Command> commands) throws IOException {
        if (commands.contains(Command.EXIT)) {
            System.exit(0);
        }

        // Handle hyperspace requests
        if (commands.contains(Command.HYPERSPACE)) {
            System.out.println("commands contain hyperspace");
            if (hyperCoolDown == 0 && serverManager.hyper()) {
                currentActors.clear();
                hyperCoolDown = HYPERPERIOD;
            }
            // Do not send the hyperspace command to the server (ever)
            commands.remove(Command.HYPERSPACE);
        }
        if (hyperCoolDown > 0) {
            hyperCoolDown--;
        }
        server.send(commands);
    }


    /**
     * Retrieves headers and actor streams from the connection to
     * the current server
     * @throws IOException If there is a communication failure
     */
    private void receiveState() throws IOException {
        final int numActors = server.receiveHeaders(clientNames);
        for (int i = 0; i < numActors; i++) {
            final int actorID = server.receiveActor(actorBuffer, i);
            if (currentActors.containsKey(actorID)) {
                currentActors.get(actorID).updateFromStream(actorBuffer);
                nextActors.put(actorID, currentActors.get(actorID));
            } else {
                final ActorType type = server.getActorType(i);
                nextActors.put(actorID, Actor.fromBuffer(type, actorID, actorBuffer));
            }
        }
    }

    /**
     * Loads the current game state into the display
     * and requests a repaint.
     */
    private void updateDisplay() {
        display.loadActors(nextActors.values());
        final Map<Integer, Actor> temp = currentActors;

        // Flip the actor buffers
        currentActors = nextActors;
        nextActors = temp;
        nextActors.clear();
        display.setServerNames(
                serverManager.getNames(),
                serverManager.getCurrentIndex());

        display.setClientNames(clientNames);
        clientNames.clear();

        display.repaint();
    }
}
