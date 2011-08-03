package client;

import common.Actor;
import common.Connection.Server;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.LinkedList;
import javax.swing.JFrame;
import javax.swing.Timer;

/**
 * A Display will paint a given set of actors and list server names.
 *
 * @author Simon, Daniel
 */
class Display extends Canvas {
    private static final int VERT_TEXT_INCR = 20;

    private BufferedImage offscreen; // Used to construct game view
    private Graphics2D offgraphics;  // Used to construct game view
    private static final String appName = "Spacewar(s)!";
    private static final Dimension appSize = new Dimension(500, 500);
    // Tracks all objects currently in the game-space.
    private LinkedList<Actor> actors = new LinkedList<Actor>();
    private LinkedList<String> serverNames = new LinkedList<String> ();
    private int currentServer = 0;
    private int selectedServer = 0;
    private final Collection<String> clientNames;

    /**
     * Create a new Display of the given size. Needs to match up to the
     * game engine's size.
     * @param listener The input handler that the client is using
     * @param size the size of the game
     */
    Display(final Dimension size, final KeyListener listener) {
        super();
        setPreferredSize(appSize);
        setMinimumSize(appSize);
        setMaximumSize(appSize);
        offscreen = new BufferedImage(size.width, size.height,
                BufferedImage.TYPE_INT_ARGB);
        offgraphics = offscreen.createGraphics();
        JFrame frame = new JFrame(appName);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(appSize);
        frame.setResizable(false);
        frame.addKeyListener(listener);
        this.addKeyListener(listener);
        frame.getContentPane().add(this);
        frame.pack();
        frame.setVisible(true);
        clientNames = new LinkedList<String>();
    }

    /**
     * Loads a list of actors into the Display's private list for thread safety
     * @param newActors
     */
    synchronized void loadActors(Collection<Actor> newActors) {
        actors.clear();
        actors.addAll(newActors);
    }

    /**
     * Render the game.
     * @param g the Graphics object to draw on
     */
    public void paint(Graphics g) {
        update(g);
    }

    /**
     * Render the game.
     * @param g the Graphics object to draw on
     */
    public synchronized void update(Graphics g) {
        // Clear the offscreen image
        offgraphics.setColor(Color.BLACK);
        offgraphics.fillRect(0, 0, getSize().width, getSize().height);

        // Render objects
        for (Actor actor : actors) {
            actor.draw(offgraphics);
        }


        offgraphics.setColor(Color.RED);
        int i = 0;
        for (String name : serverNames) {
            if (i == currentServer) {
                offgraphics.setPaint(Color.RED);
            } else if (i == selectedServer) {
                offgraphics.setPaint(Color.YELLOW);
            } else {
                offgraphics.setPaint(Color.WHITE);
            }
            offgraphics.drawString(name, appSize.width - 100, 20*i + 10);
            i++;
        }

        offgraphics.setPaint(Color.WHITE);
        i = 20;
        for (String name : clientNames) {
            offgraphics.drawString(name, 10, i);
            i += VERT_TEXT_INCR;
        }


        // Update onscreen image
        g.drawImage(offscreen, 0, 0, null);

    }

    /**
     * Pass in a set of server names which the display will make its own copy of.
     * @param servers The list of server names
     * @param current The server to highlight as being the current server
     * @param selected The server to highlight as being the next hyperspace target
     */
    synchronized void setServerNames(Collection<String> servers, int current, int selected) {
        currentServer = current;
        selectedServer = selected;
        serverNames.clear();
        serverNames.addAll(servers);
    }

    /**
     * Empties the given collection of names into this object's own container for thread safety
     * @param clientNames The list of strings to print as clients connected to the current server
     */
    synchronized void setClientNames(Collection<String> clientNames) {
        this.clientNames.clear();
        this.clientNames.addAll(clientNames);
        clientNames.clear();
    }

}
