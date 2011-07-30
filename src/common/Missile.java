package common;

import java.awt.*;
import java.nio.DoubleBuffer;
import javax.vecmath.Vector2d;

/**
 * A missile is a weapon fired by a spacecraft. Despite the name, it's actually
 * a ballistic projectile once its thruster has fired to provide an
 * initial acceleration. Missiles don't arm until the reach a predefined
 * distance from the launching spacecraft, to prevent them from killing their
 * launcher. Each missile has a finite lifetime, after which it destroys itself.
 *
 * @author Simon, Daniel, AIM
 */
public class Missile extends Actor {

    // Missiles are just drawn as simple boxes at the moment.
    private static Polygon shape = new Polygon(new int[] {8, 12, 12, 8},
                                               new int[] {8, 8, 12, 12},
                                               4);

    // The delta-V provided by the missile thruster when it fires
    private static final double IMPULSE = 10.0;
    // The distance from the spacecraft the missile has to be before it becomes
    // active
    private static final double IGNITION_DISTANCE = 20.0;

    // The number of time-steps the missile will stay active.
    private static final int INIT_LIFE = 50;

    // Remaining number of time-steps before the missile becomes inactive.
    private int lifetime = INIT_LIFE;

    private static final double DEFAULT_G = 0.001;
    private static final int MISSILE_CRASH_EFFECT = 1;
    private static final double MAX_MISSLE_VEL = 50;


    /**
     * Launch a missile from a spacecraft with the specified position and
     * heading. The missile will start near the spacecraft, and have a velocity
     * that is the vector sum of the current spacecraft velocity and the
     * launch delta-V of the missile applied in the direction the spacecraft
     * is currently pointed.
     *
     * @param initPos spacecraft position
     * @param initV spacecraft velocity
     * @param heading spacecraft orientation
     */
    public Missile(Vector2d initPos, Vector2d initV, double heading) {
        super(initPos, initV);
        spriteGraphics.setColor(Color.WHITE);
        spriteGraphics.drawPolygon(shape);
        size = new Dimension(4, 4);

        // Move the missile position to the launch distance
        initPos.add(new Vector2d(IGNITION_DISTANCE*Math.cos(heading),
                                 IGNITION_DISTANCE*Math.sin(heading)));
        this.setPosition(initPos);

        // Determine the missile velocity after launch
        Vector2d launch_accel = new Vector2d(IMPULSE*Math.cos(heading),
                                             IMPULSE*Math.sin(heading));
        this.accelerate(launch_accel);
        super.stepTime(); // Force a position update
    }

    Missile(int id, double[] buffer) {
        super(id, buffer);
        spriteGraphics.setColor(Color.getHSBColor((float)buffer[0], 1f, 1f));
        spriteGraphics.drawPolygon(shape);
    }

    /**
     * Update the missile position and velocity, and check to see if the
     * missile has exceeded its lifetime.
     */
    @Override
    public void stepTime() {
        super.stepTime();
        --lifetime;
        if (lifetime <= 0) {
            this.destroy();
        }
    }

    @Override
    public double getMaxVel(){
        return MAX_MISSLE_VEL;
    }

    @Override
    public int getActorType() {
        return ActorType.MISSILE.ordinal();
    }

    @Override
    public int getCollisionDamage() {
        return MISSILE_CRASH_EFFECT;
    }

}