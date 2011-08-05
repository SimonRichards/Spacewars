package common;

import java.awt.*;
import javax.vecmath.Vector2d;

/**
 * A spacecraft is the game player's representative in the game space.
 * Spacecraft are able to maneuver and shoot at each other.
 *
 * @author AIM, Simon, Daniel
 */
public abstract class Spacecraft extends Actor {

    // delta-V provided by one thrust command
    private static final double IMPULSE = 2.0;

    // Change in orientation provided by one rotate command
    private static final double TURN_INCREMENT = 0.3;

    private int timeTillCool;
    private static final int COOLDOWN_TIME = 5;

    private int shields = 4;   // Number of hits the spacecraft can take
    private static final int SHIP_CRASH_EFFECT = 1;


    /**
     * Launch a missile
     * @return The new missile object
     */
    public Missile fire() {
        Missile missle = null;
        if(timeTillCool < 0){
            Vector2d vel = new Vector2d(getVelocity());
            missle = new Missile(getPosition(), vel, getHeading());
            timeTillCool = COOLDOWN_TIME;
        }
        return missle;
    }

    /**
     * @inheritDoc
     * Also allows the Spacecraft's Missile launcher to cool down a little
     */
    @Override
    public void stepTime(){
        super.stepTime();
        timeTillCool--;
    }

    /**
     * Rebuilds a Spacecraft from a buffer
     * @param actorID The actorID of the new spacecraft
     * @param buffer The buffer to build from
     */
    Spacecraft(int id, double[] buffer) {
        super(id, buffer);
        timeTillCool = 0;
    }



    /**
     * Create a new spacecraft with the specified position and velocity.
     * @param pos initial spacecraft position
     * @param vel initial spacecraft velocity
     */
    public Spacecraft(int id, Vector2d pos, Vector2d vel) {
        super(id, pos, vel);
        timeTillCool = 0;
    }

    /**
     *
     * @param pos
     * @param vel
     */
    public Spacecraft(Vector2d pos, Vector2d vel) {
        super(pos, vel);
        timeTillCool = 0;
    }

    /**
     * Damaging a spacecraft reduces its shield levels. When shield levels
     * reach 0 the spacecraft is destroyed.
     */
    @Override
    public void damage(int damageTaken) {
        shields -= damageTaken;
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
     * @inheritDoc
     */
    @Override
    public int getCollisionDamage(){
        return SHIP_CRASH_EFFECT;
    }

    /**
     * The Needle is one of the two classic Spacewar spacecraft.
     * In this case it's simply a spacecraft with a long thin fuselage.
     */
    public static class Needle extends Spacecraft {
        private static Polygon shape = new Polygon(new int[] {0, 4, 0, 20},
                                    new int[] {6, 10, 14, 10},
                                    4);
        public Needle(Vector2d position, Vector2d velocity) {
            super(position, velocity);
            rotate(Math.toRadians(135.0));
            setSprite();
        }

        Needle(double[] buffer) {
            super(0, buffer);
            setSprite();
        }

        @Override
        public int getActorType() {
            return ActorType.NEEDLE.ordinal();
        }

        private void setSprite() {

            spriteGraphics.setColor(Color.decode("0xAFD775"));
            spriteGraphics.fillPolygon(shape);
        }
    }

    /**
     * The Wedge is one of the two classic Spacewar spacecraft.
     * In this case it's simply a spacecraft with a wide triangular fuselage.
     */
    public static class Wedge extends Spacecraft {
        private static Polygon shape = new Polygon(new int[] {3, 15, 3, 0},
                                    new int[] {3, 10, 17, 10},
                                    4);
        public Wedge(int id, Vector2d position, Vector2d velocity) {
            super(id, position, velocity);
            colour = Math.abs((double)id / Integer.MAX_VALUE);
            rotate(Math.toRadians(45.0));
            setSprite();
        }


        Wedge(int id, double[] buffer) {
            super(id, buffer);
            setSprite();
        }

        private void setSprite() {
            spriteGraphics.fillPolygon(shape);
        }

        @Override
        public int getActorType() {
            return ActorType.WEDGE.ordinal();
        }


    }


}

