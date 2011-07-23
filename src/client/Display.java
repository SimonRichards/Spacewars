package client;

import client.InputHandler.SpacecraftController;
import common.Spacecraft;
import common.Star;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.Timer;
import javax.vecmath.Vector2d;

/**
 *
 * @author Simon
 */
public class Display {
    private BufferedImage offscreen; // Used to construct game view
    private Graphics2D offgraphics;  // Used to construct game view
    private Timer timer;             // Game update timer

    // Tracks all objects currently in the game-space.
    private ArrayList<SpacewarObject> objects = new ArrayList<SpacewarObject>();
        
    private static final double FRAME_RATE = 24.0; // Frames per second
    /**
     * Create a new Spacewar game-space of the specified size.
     * @param size the size of the game
     */
    public SpacewarGame(Dimension size) {
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

        // Central star        
        this.addObject(new Star(new Vector2d(0.5*size.width, 0.5*size.height),
                                1000.0));
     
        // The initial spacecraft
        Spacecraft s1 = new Spacecraft.Wedge();
        s1.setGame(this);
        s1.setPosition(new Vector2d(20.0, 20.0));
        s1.setVelocity(new Vector2d(-1.0, 0));    
        this.addObject(s1);
        

        Spacecraft s2 = new Spacecraft.Needle();
        s2.setGame(this);
        s2.setPosition(new Vector2d(400.0, 400.0));
        s2.setVelocity(new Vector2d(1.0, -1.0));
        this.addObject(s2);     
        
        // Set up keyboard control
        this.addKeyListener(new SpacecraftController(s1, 
                                KeyEvent.VK_A, KeyEvent.VK_D, 
                                KeyEvent.VK_S, KeyEvent.VK_W));        
        this.addKeyListener(new SpacecraftController(s2, 
                                KeyEvent.VK_J, KeyEvent.VK_L, 
                                KeyEvent.VK_K, KeyEvent.VK_I));  
                                
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
        for (SpacewarObject obj : objects) {
            obj.draw(offgraphics);
        }
        
        // Update onscreen image
        g.drawImage(offscreen, 0, 0, null);
    }
        
}
