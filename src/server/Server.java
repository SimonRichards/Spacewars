package server;

import common.Actor;
import common.Command;
import common.Connection;
import common.Connection.Client;
import common.Game;
import common.Missile;
import common.Spacecraft;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Simon, Daniel
 */
public class Server extends TimerTask {

    private final Collection<Connection.Client> clients;
    private Connection.Client localClient;
    private final GameEngine engine;
    private final Map<Connection.Client, Spacecraft> spacecraftFromClient;
    private static final int MAX_CLIENTS = 10;
    private final boolean standalone;
    private boolean firstTime;
    private final int port;
    private final ClientListener listener;
    private final int[] commandBuffer;
    private final double[] actorBuffer;

    /**
     * Starts a new Server object and schedules its loop for periodic execution
     * @param tcpPort The port to use
     * @param headless Whether or not to crete the server standalone
     * @throws IOException If the socket cannot be bound to
     */
    public static void start(final int tcpPort, final boolean headless) throws IOException {
        new Timer().scheduleAtFixedRate(new Server(tcpPort, headless), 0, Game.GAME_PERIOD);
    }

    /**
     * Creates a new server on the specified port
     * @param port Socket number to use
     * @param standalone
     * @throws IOException if the Server cannot be created
     */
    private Server(final int port, final boolean standalone) throws IOException {
        super();
        commandBuffer = new int[Game.COMMAND_BUFFER_SIZE];
        actorBuffer = new double[Actor.NUM_ELEMENTS];
        engine = new GameEngine();
        clients = new LinkedList<Connection.Client>();
        spacecraftFromClient = new ConcurrentHashMap<Client, Spacecraft>(MAX_CLIENTS);
        this.standalone = standalone;
        this.port = port;
        listener = new ClientListener(port);
    }

    /**
     * Main loop for the server
     */
    @Override
    public void run() {
        // First time set up code
        if (!firstTime && !standalone) {
            findLocalClient();
        }

        handleClientRequests();

        for (Command command : engine.aiActor.update(engine.actors)) {
            handleCommand(engine.aiActor, command);
        }

        engine.stepTime();

        transmitState();

        listener.loadNewClients(clients);
    }

    private void findLocalClient() {
        localClient = listener.blockUntilClient();
        clients.add(localClient);
        addActorfromClient(localClient);
        new ServerAdvertiser(port);
        new Thread(listener).start();
        firstTime = true;
    }

    /**
     * Modifies the given spacecraft from a command. Not delegated due to extra
     * handling of missiles.
     * @param spacecraft The spacecraft to apply the command to
     * @param input The command sent by the client
     */
    private void handleCommand(final Spacecraft spacecraft, final Command input) {
        switch (input) {
            case FORWARD:
                spacecraft.accelerate(0.5);
                break;
            case TURN_CCW:
                spacecraft.counterClockwise();
                break;
            case TURN_CW:
                spacecraft.clockwise();
                break;
            case FIRE:
                if (!spacecraft.isDead()) {
                    Missile missile = spacecraft.fire();
                    if (missile != null) {
                        engine.actors.add(missile);
                    }
                }
                break;
            default:
                System.err.println("Input fallthrough" + input.toString());
                System.exit(-1);
        }
    }

    /**
     * Creates a new Wedge spacecraft, adds it to the engine and maps it
     * to the client in the spacecraftFromClient map.
     * @param client The client to map to
     */
    private void addActorfromClient(final Connection.Client client) {
        spacecraftFromClient.put(client, engine.addSpaceship(client.getID()));
    }

    /**
     * Removes the client and their
     * @param client
     */
    private void removeClient(final Client client) {
        final Actor clientActor = spacecraftFromClient.get(client);
        if (clientActor != null) {
            clientActor.destroy();
        }
        spacecraftFromClient.remove(client);
        clients.remove(client);
    }

    /**
     * Acts upon all requests from all connected clients,
     * even those not currently in the game.
     */
    private void handleClientRequests() {
        Command input;
        int numCommands;
        for (Connection.Client client : clients) {
            try {
                numCommands = client.getCommands(commandBuffer);
                for (int i = 0; i < numCommands; i++) {
                    input = Command.fromInt(Integer.valueOf(commandBuffer[i]));
                    // Apply the command
                    switch (input) {
                        case EXIT:
                            engine.actors.remove(spacecraftFromClient.get(client));
                            spacecraftFromClient.remove(client);
                            break;
                        case ENTRY:
                            if (spacecraftFromClient.get(client).isDead()) {
                                addActorfromClient(client);
                            }
                            break;
                        default:
                            if (spacecraftFromClient.get(client) != null) {
                                handleCommand(spacecraftFromClient.get(client), input);
                            }
                    }
                }
            } catch (IOException e) {
                removeClient(client);
            }
        }
    }

    /**
     * Transmits the entire gamestate to all currently playing clients
     */
    private void transmitState() {
        //transmit the header to each client
        for (Connection.Client client : spacecraftFromClient.keySet()) {
            try {
                client.sendHeader(engine.actors, spacecraftFromClient.keySet());
            } catch (IOException e) {
                removeClient(client);
            }
        }

        // Transmit the actor list to each client
        for (Actor actor : engine.actors) {
            //calculate a stream for each actor only once, then transmit to each client
            actor.toStream(actorBuffer);
            for (Connection.Client client : spacecraftFromClient.keySet()) {
                try {
                    client.sendActor(actorBuffer);
                } catch (IOException e) {
                    removeClient(client);
                }
            }
        }
    }
}
