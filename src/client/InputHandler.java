package client;

import common.Spacecraft;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Simon
 */
public class InputHandler {
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
