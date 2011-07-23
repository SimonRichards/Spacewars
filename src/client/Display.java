package client;

import client.InputHandler.SpacecraftController;
import common.Actor;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.Timer;

/**
 *
 * @author Simon
 */
public class Display extends Canvas{
    private BufferedImage offscreen; // Used to construct game view
    private Graphics2D offgraphics;  // Used to construct game view
    private Timer timer;             // Game update timer
    private static final String appName = "Spacewar(s)!";
    private static final Dimension appSize = new Dimension(500, 500);

    // Tracks all objects currently in the game-space.
    private ArrayList<Actor> objects = new ArrayList<Actor>();
        
    private static final double FRAME_RATE = 24.0; // Frames per second
    /**
     * Create a new Spacewar game-space of the specified size.
     * @param size the size of the game
     */
    public Display(Dimension size) {
        
        
        // Create and set up the window.
        JFrame frame = new JFrame(appName);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Specify the size
        frame.setSize(appSize);
    
        // Make the game get the focus when the frame is activated.
        frame.addWindowFocusListener(new WindowAdapter() {
            public void windowGainedFocus(WindowEvent e) {
                requestFocusInWindow();
            }
        });

        // Display the window.
        frame.pack();
        frame.setVisible(true);
        
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        
        // Set up display        
        offscreen = new BufferedImage(size.width, size.height, 
                                      BufferedImage.TYPE_INT_ARGB);
        offgraphics = offscreen.createGraphics();

        // Define the initial game state. This could probably be placed
        // in a config file (or randomly generated) in a more comprehensive
        // implementation.

   
        /*
        // Set up keyboard control
        this.addKeyListener(new SpacecraftController(s1, 
                                KeyEvent.VK_A, KeyEvent.VK_D, 
                                KeyEvent.VK_S, KeyEvent.VK_W));        
        this.addKeyListener(new SpacecraftController(s2, 
                                KeyEvent.VK_J, KeyEvent.VK_L, 
                                KeyEvent.VK_K, KeyEvent.VK_I));  
                                */
        // Set up timer to drive animation events.
        int milliseconds_per_frame = (int)(1000.0/FRAME_RATE);
        timer = new Timer(milliseconds_per_frame, (ActionListener)this);
        timer.start();        
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
    public void update(Graphics g){
        // Clear the offscreen image
        offgraphics.setColor(Color.BLACK);
        offgraphics.fillRect(0, 0, getSize().width, getSize().height); 
        
        // Render objects
        for (Actor obj : objects) {
            obj.draw(offgraphics);
        }
        
        // Update onscreen image
        g.drawImage(offscreen, 0, 0, null);
    }
        
}
