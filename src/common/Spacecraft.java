package common;

import java.awt.*;
import javax.vecmath.Tuple2d;
import javax.vecmath.Vector2d;

/**
 * A spacecraft is the game player's representative in the game space.
 * Spacecraft are able to maneuver and shoot at each other.
 *
 * @author Simon, Daniel, AIM
 */
public abstract class Spacecraft extends Actor {

    // delta-V provided by one thrust command
    private static final double IMPULSE = 2.0;

    // Change in orientation provided by one rotate command
    private static final double TURN_INCREMENT = 0.1;

    private int cooldown;
    private static final int COOLDOWN_TIME = 3;

    private int shields = 4;   // Number of hits the spacecraft can take

    public boolean isCooledDown() {
        return cooldown == 0;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    /**
     * Launch a missile
     * @return The new missile object
     */
    public Missile fire() {
//        if(isCooledDown())
        Vector2d vel = new Vector2d(getVelocity());
        return new Missile(getPosition(), vel, getHeading());
    }

    Spacecraft(String stream) {
        super(stream);
    }

    /**
     * Create a new spacecraft with the specified position and velocity.
     * @param initPos initial spacecraft position
     * @param initV initial spacecraft velocity
     */
    public Spacecraft(Vector2d pos, Vector2d vel, int colourInt) {
        super(pos, vel, colourInt);
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
     * The Needle is one of the two classic Spacewar spacecraft.
     * In this case it's simply a spacecraft with a long thin fuselage.
     */
    public static class Needle extends Spacecraft {

        public Needle(Vector2d position, Vector2d velocity) {
            super(position, velocity, 0);
            rotate(Math.toRadians(135.0));
            setSprite();
        }

        Needle(String stream) {
            super(stream);
            setSprite();
        }

        @Override
        public int getID() {
            return ActorType.NEEDLE.ordinal();
        }

        private void setSprite() {
            Polygon shape = new Polygon(new int[] {0, 4, 0, 20},
                                        new int[] {6, 10, 14, 10},
                                        4);
            spriteGraphics.setColor(Color.getHSBColor(0, 1.0f, 1.0f));
            spriteGraphics.drawPolygon(shape);
        }
    }

    /**
     * The Wedge is one of the two classic Spacewar spacecraft.
     * In this case it's simply a spacecraft with a wide triangular fuselage.
     */
    public static class Wedge extends Spacecraft {
        public Wedge(Vector2d position, Vector2d velocity, int colourInt) {
            super(position, velocity, colourInt);
            rotate(Math.toRadians(45.0));
            setSprite();
        }

        Wedge(String stream) {
            super(stream);
            setSprite();
        }

        private void setSprite() {
            Polygon shape = new Polygon(new int[] {0, 15, 0},
                                        new int[] {3, 10, 17},
                                        3);
            spriteGraphics.drawPolygon(shape);
        }

        @Override
        public int getID() {
            return ActorType.WEDGE.ordinal();
        }


    }


}

