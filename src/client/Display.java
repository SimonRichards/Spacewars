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
 *
 * @author Simon, Daniel
 */
public class Display extends Canvas {

    private BufferedImage offscreen; // Used to construct game view
    private Graphics2D offgraphics;  // Used to construct game view
    private static final String appName = "Spacewar(s)!";
    private static final Dimension appSize = new Dimension(500, 500);
    // Tracks all objects currently in the game-space.
    private LinkedList<Actor> actors = new LinkedList<Actor>();
    private LinkedList<String> serverNames = new LinkedList<String> ();

    /**
     * Create a new Spacewar game-space of the specified size.
     * @param listener
     * @param size the size of the game
     */
    public Display(final Dimension size, final KeyListener listener) {
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
        frame.addKeyListener(listener);
        this.addKeyListener(listener);
        frame.getContentPane().add(this);
        frame.pack();
        frame.setVisible(true);
    }

    public synchronized void loadActors(Collection<Actor> newActors) {
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
            offgraphics.drawString(name, appSize.width - 100, 20*i + 10);
            i++;
        }

        // Update onscreen image
        g.drawImage(offscreen, 0, 0, null);

    }

    void setServerNames(LinkedList<Server> servers) {
        serverNames.clear();
        for (Server server : servers) {
            serverNames.add(server.getName());
        }
    }

}
