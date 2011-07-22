package spacewar;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.Timer;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import javax.vecmath.Vector2d;

/**
 * Coordinates the behaviour of the game and displays the current
 * game state on screen. Keypresses directed at the game object 
 * are delegated to one or more SpacecraftControllers for
 * interpretation as Spacecraft commands.
 *
 * Since this is a small example, combining display and state updates
 * in one object is probably ok. But in a bigger and more complex
 * game we'd probably separate those responsibilities out into
 * their own classes.
 */
public class SpacewarGame extends Canvas implements ActionListener {
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
    
    /**
     * Move the game state forward by one time-step. The state
     * update checks for collisions generated in the previous
     * step, applies gravitational forces between all objects, and
     * removes any objects that have somehow become dead.
     */
    public void stepTime() {
        // Look at every pair of objects to apply mutual forces 
        // and detect collisions. 
        for (int i = 0; i < objects.size(); ++i) {
            SpacewarObject obj = objects.get(i);
            for (int j = i+1; j < objects.size(); ++j) {
                SpacewarObject other = objects.get(j);
                obj.gravitate(other);                
                if (obj.hasCollidedWith(other)) {
                    obj.damage();
                    other.damage();                   
                }
            }
        } 
        
        // Update positions and mark all dead objects
        ArrayList<SpacewarObject> deadObjects = new ArrayList<SpacewarObject>();
        for (SpacewarObject obj : objects) {
            obj.stepTime();
            if (obj.isDead()) {
                deadObjects.add(obj);
            }
        }
        
        // Remove dead objects
        for (SpacewarObject obj : deadObjects) {
            objects.remove(obj);
        }
    }
    
    /**
     * Add a new object to the game space.
     * @param obj the new object
     */
    public void addObject(SpacewarObject obj) {
        objects.add(obj);
        repaint();
    }
    
    /**
     * Respond to a timer event by updating the game state.
     * @param e the received event (which is ignored in this case)
     */
    public void actionPerformed(ActionEvent e) {
        stepTime();
        repaint();
    }
    
    /**
     * Nested class define a reusable keyboard controller for
     * a spacecraft. Essentially provides a mapping between
     * key-presses and spacecraft commands.
     */
    class SpacecraftController extends KeyAdapter {
        private Spacecraft sc; // Spacecraft being controlled
        
        // Defines the relationship between keys and spacecraft commands
        private Map<Integer,Spacecraft.Command> commandFromKey = 
            new HashMap<Integer,Spacecraft.Command>();
        
        /**
         * Create a new controller for the given spacecraft. The controller
         * will translate each of the specified keys into the corresponding
         * command.
         *
         * Example: new SpacecraftController(s,
         *                       KeyEvent.VK_A, KeyEvent.VK_D, 
         *                       KeyEvent.VK_S, KeyEvent.VK_W)
         *
         * Sets up a controller for spacecraft s using keys 'A', 'D'
         * 'S', and 'W'.
         *
         * @param spacecraft the spacecraft to control
         * @param counterClockwise key to rotate spacecraft left
         * @param clockwise key to rotate spacecraft right
         * @param thrust key to fire spacecraft thrusters
         * @param fire key to fire spacecraft weapons
         */
        public SpacecraftController(Spacecraft spacecraft,
                                    Integer counterClockwise, 
                                    Integer clockwise, 
                                    Integer thrust,
                                    Integer fire) {
            this.sc = spacecraft;
            commandFromKey.put(counterClockwise,
                Spacecraft.Command.COUNTER_CLOCKWISE);
            commandFromKey.put(clockwise, Spacecraft.Command.CLOCKWISE);
            commandFromKey.put(thrust, Spacecraft.Command.THRUST);
            commandFromKey.put(fire, Spacecraft.Command.FIRE);            
        }
        
        /**
         * Accepts a keypress event and executes the corresponding command
         * on the controlled spacecraft.
         * @param k the pressed key
         */
        public void keyPressed(KeyEvent k) {
            Spacecraft.Command c = commandFromKey.get(k.getKeyCode());
            if (c != null && !sc.isDead()) {
                c.execute(sc);
            }
        }  
    }
}
