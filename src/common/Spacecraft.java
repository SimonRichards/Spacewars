package common;

import java.awt.*;
import javax.vecmath.Vector2d;

/**
 * A spacecraft is the game player's representative in the game space.
 * Spacecraft are able to maneuver and shoot at each other.
 */
public class Spacecraft extends SpacewarObject {

    // delta-V provided by one thrust command
    private static final double IMPULSE = 2.0; 
    
    // Change in orientation provided by one rotate command
    private static final double TURN_INCREMENT = 0.1;
    
    private int shields = 4;   // Number of hits the spacecraft can take
    private SpacewarGame game; // Containing game-space. 

    /**
     * Create a new spacecraft with default position and velocity.
     */
    public Spacecraft() {
        super();
    }
    
    /**
     * Create a new spacecraft with the specified position and velocity.
     * @param initPos initial spacecraft position
     * @param initV initial spacecraft velocity
     */    
    public Spacecraft(Vector2d initPos, Vector2d initV) {
        super(initPos, initV);
    }
    
    /** 
     * Damaging a spacecraft reduces its shield levels. When shield levels 
     * reach 0 the spacecraft is destroyed.
     */
    @Override
    public void damage() {
        --shields;
        if (shields < 1) {
            this.destroy();
        }
    }
    
    /**
     * Fire the spacecraft thrusters to provide a change in velocity (delta-V).
     */
    public void thrust() {
        accelerate(IMPULSE);
    }
    
    /**
     * Rotate the spacecraft clockwise.
     */
    public void clockwise() {
        rotate(TURN_INCREMENT);
    }
    
    /**
     * Rotate the spacecraft counter-clockwise.
     */    
    public void counterClockwise() {
        rotate(-TURN_INCREMENT);
    }
    
    /**
     * Define the game-space in which the spacecraft is currently
     * contained.
     * @param game the containing game-space
     */
    public void setGame(SpacewarGame game) {
        this.game = game;
    }
    
    /** 
     * Launch a missile
     */
    public void fire() {
        if (game != null) {
            game.addObject(new Missile(this.getPosition(), 
                                       this.getVelocity(), 
                                       this.getHeading()));
        }
    }
    
    /**
     * The Needle is one of the two classic Spacewar spacecraft.
     * In this case it's simply a spacecraft with a long thin fuselage.
     */
    public static class Needle extends Spacecraft {
        public Needle() {
            rotate(Math.toRadians(135.0));
            Polygon shape = new Polygon(new int[] {0, 4, 0, 20},
                                        new int[] {6, 10, 14, 10},
                                        4);                               
            spriteGraphics.setColor(Color.WHITE);
            spriteGraphics.drawPolygon(shape);
        }
    }   
    
    /**
     * The Wedge is one of the two classic Spacewar spacecraft.
     * In this case it's simply a spacecraft with a wide triangular fuselage.
     */    
    public static class Wedge extends Spacecraft {
        public Wedge() {
            rotate(Math.toRadians(45.0));
            Polygon shape = new Polygon(new int[] {0, 15, 0},
                                        new int[] {3, 10, 17},
                                        3);                               
            spriteGraphics.setColor(Color.WHITE);
            spriteGraphics.drawPolygon(shape);
        }
    }      
    
    /**
     * Nested enum used to define a spacecraft command interface for
     * use with external controllers. This is something like a 
     * lightweight implementation of the Command pattern.
     */
    public static enum Command { 
        COUNTER_CLOCKWISE { void execute(Spacecraft sc){ 
                                sc.counterClockwise();
                            } }, 
        CLOCKWISE { void execute(Spacecraft sc){ sc.clockwise();} }, 
        THRUST { void execute(Spacecraft sc){ sc.thrust();} },
        FIRE { void execute(Spacecraft sc){ sc.fire();} };

        abstract void execute(Spacecraft sc);
    };    
}

