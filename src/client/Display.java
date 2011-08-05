package client;

import common.Actor;
import common.Game;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JFrame;

/**
 * A Display will paint a given set of actors and list server names.
 *
 * @author Simon, Daniel
 */
class Display extends Canvas {

    private static final int VERT_TEXT_INCR = 20;
    private final BufferedImage offscreen; // Used to construct game view
    private final Graphics2D offgraphics;  // Used to construct game view
    private static final String APPNAME = "Spacewar(s)!";
    private final List<Actor> actors;
    private final List<String> serverNames;
    private int currentServer = 0;
    private final Collection<String> clientNames;

    /**
     * Create a new Display of the given size. Needs to match up to the
     * game engine's size.
     * @param listener The input handler that the client is using
     * @param size the size of the game
     */
    Display(final Dimension size, final KeyListener listener) {
        super();
        actors = new LinkedList<Actor>();
        serverNames = new ArrayList<String>(Game.APPSIZE.height / VERT_TEXT_INCR);
        clientNames = new ArrayList<String>(Game.APPSIZE.height / VERT_TEXT_INCR);
        setPreferredSize(Game.APPSIZE);
        setMinimumSize(Game.APPSIZE);
        setMaximumSize(Game.APPSIZE);
        offscreen = new BufferedImage(size.width, size.height,
                BufferedImage.TYPE_INT_ARGB);
        offgraphics = offscreen.createGraphics();
        final JFrame frame = new JFrame(APPNAME);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(Game.APPSIZE);
        frame.setResizable(false);
        frame.addKeyListener(listener);
        this.addKeyListener(listener);
        frame.getContentPane().add(this);
        frame.pack();
        frame.setVisible(true);
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
     * @param graphics the Graphics object to draw on
     */
    public void paint(final Graphics graphics) {
        update(graphics);
    }

    /**
     * Render the game.
     * @param graphics the Graphics object to draw on
     */
    public void update(final Graphics graphics) {
        // Clear the offscreen image
        offgraphics.setColor(Color.BLACK);
        offgraphics.fillRect(0, 0, getSize().width, getSize().height);

        synchronized (this) {
            // Render objects
            for (Actor actor : actors) {
                actor.draw(offgraphics);
            }


            offgraphics.setColor(Color.RED);
            int i = 0;
            for (i = 0; i < serverNames.size(); i++) {
                final Color color = (i == currentServer) ? Color.RED : Color.WHITE;
                offgraphics.setPaint(color);
                offgraphics.drawString(serverNames.get(i), Game.APPSIZE.width - 100, 20 * i + 10);
            }

            offgraphics.setPaint(Color.WHITE);
            i = 20;
            for (String name : clientNames) {
                offgraphics.drawString(name, 10, i);
                i += VERT_TEXT_INCR;
            }
        }

        // Update onscreen image
        graphics.drawImage(offscreen, 0, 0, null);
    }

    /**
     * Pass in a set of server names which the display will make its own copy of.
     * @param servers The list of server names
     * @param current The server to highlight as being the current server
     * @param selected The server to highlight as being the next hyperspace target
     */
    synchronized void setServerNames(Collection<String> servers, int current) {
        currentServer = current;
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
