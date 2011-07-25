package server;

import common.Actor;
import common.Command;
import common.Connection;
import common.Game;
import common.Spacecraft;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Simon, Daniel
 */
public class Server extends TimerTask {

    private ServerSocket serverSocket;
    private final Collection<Connection.Client> clients;
    private Connection.Client localClient;
    private GameEngine engine;
    private final ArrayList<Connection.Client> clientBuffer;
    private final Map<Connection.Client, Spacecraft> spacecraftFromClient;
    private static final int MAX_CLIENTS = 10;
    private boolean standalone;
    private boolean firstTime;
    private final AI ai;

    /**
     * Creates a new server on the specified port
     * @param port Socket number to use
     * @param standalone
     * @throws IOException if the Server cannot be created
     */
    public Server(int port, boolean standalone) throws IOException {
        serverSocket = new ServerSocket(port);
        engine = new GameEngine();
        clients = new LinkedList<Connection.Client>();
        clientBuffer = new ArrayList<Connection.Client>(10);
        spacecraftFromClient = new HashMap<Connection.Client, Spacecraft>(MAX_CLIENTS);
        ai = new AI(Collections.unmodifiableCollection(engine.actors));
        this.standalone = standalone;
    }

    /**
     * Main loop for the server
     */
    @Override
    public void run() {
        // First time set up code
        if (!firstTime && !standalone) {
            findLocalClient();
            firstTime = true;
            UDPBroadcaster.start();
            ClientListener.start(this, serverSocket);
        }

        // Main loop
        for (Connection.Client client : clients) {
            Spacecraft spacecraft = spacecraftFromClient.get(client);
            if (spacecraft != null) {
                try {
                    String clientOutput;
                    while ((clientOutput = client.readln()) != null)  {
                        Command input = Command.fromInt(Integer.valueOf(clientOutput));
                        switch (input) {
                            case EXIT:
                                engine.actors.remove(spacecraftFromClient.get(client));
                                spacecraftFromClient.remove(this);
                                break;
                            case ENTRY:
                                addActorfromClient(client);
                                break;
                            default:
                                handleCommand(spacecraft, input);
                        }
                    }
                } catch (IOException ex) {
                    removeClient(client);
                    System.err.println("Command read failed");
                }
            }
        }

        for (Command command : ai.update()) {
            handleCommand(engine.aiActor, command);
        }

        //calculate state
        engine.stepTime();

        //transmit state
        for (Connection.Client client : clients) {
            client.writeln(engine.actors.size());
        }
        for (Actor actor : engine.actors) {
            //calculate a stream for each actor only once, then transmit to each client
            String stream = actor.toStream();
            for (Connection.Client client : clients) {
                client.writeln(stream);
            }
        }

        loadNewClients();

    }

    private void handleCommand(Spacecraft spacecraft, Command input) {
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
                    engine.actors.add(spacecraft.fire());
                }
                break;
            default:
                System.err.println("Input fallthrough" + input.toString());
                System.exit(-1);
        }
    }

    public void kill() {

        // Game over, kill the connection
        for (Connection.Client client : clients) {
            client.close();
        }
    }

    private boolean findLocalClient() {
        try {
            Socket clientSocket = serverSocket.accept();
            localClient = new Connection.Client(clientSocket);
            clients.add(localClient);
            addActorfromClient(localClient);
        } catch (IOException e) {
            System.err.println("local client not found");
            return false;
        }
        return true;
    }

    public synchronized void addClient(Connection.Client client) {
        clientBuffer.add(client);
    }

    private void addActorfromClient(Connection.Client client) {
        spacecraftFromClient.put(client, engine.addSpaceship(141)); //TODO get colourInt from client
    }

    private synchronized void loadNewClients() {
        clients.addAll(clientBuffer);
        for (Connection.Client client : clientBuffer) {
            addActorfromClient(client);
        }
        clientBuffer.clear();
    }

    private synchronized void removeClient(Connection.Client client) {
        clients.remove(client);
    }
}