package common;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.vecmath.Vector2d;
import java.awt.geom.Rectangle2D;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.Random;

/**
 * General class of all objects in the Spacewar game. Every object has
 * a position, velocity, heading angle, and a "gravity constant" that
 * acts as a proxy for mass.
 *
 * @author Simon, Daniel, AIM
 */
public abstract class Actor {
    // Maximum size of an onscreen object

    private static final int SPRITE_DIM = 20;
    // Default values for position and velocity vectors, heading, and
    // gravity constant
    private static final double DEFAULT_HEADING = Math.toRadians(1.0);
    private static final double DEFAULT_G = 1.0;
    private static final double MAX_VELOCITY = 15;
    // Image representing the object on screen, and a graphics context
    // to draw to the image.
    private BufferedImage sprite;
    protected Graphics2D spriteGraphics;
    // Size of the object in pixels
    protected Dimension size;
    // Current position and velocity in the game-space
    private ToroidalCoordinate2D position;
    protected Vector2d velocity;
    // Current heading angle in radians
    protected double angle = DEFAULT_HEADING;
    // "Gravity constant" of this object. Roughly analogous to GM, where
    // G is the newtonian gravity constant and M is the object mass.
    private double gravityConstant = DEFAULT_G;
    // True if the object is still active
    private boolean alive = true;
    protected double colour;
    protected int actorID;
    private static int idCounter = 1;
    public static final int NUM_ELEMENTS = 4;

    /**
     * Creates a new Actor with the given actorID
     * @param actorID The chosen actorID
     */
    private Actor(int id) {
        this.actorID = id;
        sprite = new BufferedImage(SPRITE_DIM, SPRITE_DIM,
                BufferedImage.TYPE_INT_ARGB);
        spriteGraphics = sprite.createGraphics();
        size = new Dimension(SPRITE_DIM, SPRITE_DIM);
    }

    /**
     * Creates a new actor with a brand new and hopefully unique ID
     */
    private Actor() {
        this(idCounter++);
    }

    /**
     * @return The unique identifier of this actor
     */
    public int getID() {
        return actorID;
    }

    /**
     * @return The actor's dimensions
     */
    public Dimension getSize() {
        return size;
    }

    /**
     * Creates an object at the specified position and velocity
     * @param id The
     * @param initPos the object position
     * @param initV the object velocity
     * gives the full range
     */
    protected Actor(int id, Vector2d initPos, Vector2d initV) {
        this(id);
        position = new ToroidalCoordinate2D(initPos);
        velocity = new Vector2d(initV);
    }

    /**
     * Creates a new actor without choosing the actorID.
     * @param pos The initial position
     * @param vel The initial Velocity
     */
    protected Actor(Vector2d pos, Vector2d vel) {
        this();
        position = new ToroidalCoordinate2D(pos);
        velocity = new Vector2d(vel);
    }

    /**
     * For client side instantiating
     * @param id the actor's id
     * @param buffer The set of doubles received from a server
     */
    protected Actor(int id, double[] buffer) {
        this(id);
        spriteGraphics.setColor(Color.getHSBColor((float) buffer[0], 0.5f, 0.5f));
        position = new ToroidalCoordinate2D(new Vector2d(buffer[1], buffer[2]));
//      velocity = new Vector2d(buffer[3], buffer[4]);
        angle = buffer[3];
    }

    /**
     * For client side updating
     * @param buffer The buffer to update from
     */
    public void updateFromStream(double[] buffer) {
        position = new ToroidalCoordinate2D(new Vector2d(buffer[1], buffer[2]));
//      velocity = new Vector2d(buffer[3], buffer[4]);
        angle = buffer[3];
    }

    /**
     * Saves the state of this actor to a buffer for transmission
     * @param buffer The buffer to save to, needs to be Actor.NUM_ELEMENTS long
     */
    public void toStream(double[] buffer) {
        int i = 0;
        buffer[i++] = colour;
        buffer[i++] = position.getX();
        buffer[i++] = position.getY();
        buffer[i++] = angle;
    }

    /**
     * @return The ordinal value of ActorType corresponding to this actor
     */
    public abstract int getActorType();

    /**
     * Renders the object sprite
     * @param graphics the Graphics2D object to draw to
     */
    public void draw(final Graphics2D graphics) {
        final AffineTransform trans = new AffineTransform();

        // Note ordering here: first transform you want to apply gets
        // added last in the sequence
        trans.translate(position.getX(), position.getY());
        trans.rotate(angle);
        trans.translate(-SPRITE_DIM / 2, -SPRITE_DIM / 2);  // Center the image
        graphics.drawRenderedImage(sprite, trans);
    }

    /**
     * Updates the position based on the current velocity.
     */
    public void stepTime() {
        position.translate(velocity); // Assumes uniform timestep
    }

    /**
     * Modifies the object velocity by some vector amount
     * @param deltaV the change in velocity
     */
    public void accelerate(final Vector2d deltaV) {
        velocity.add(deltaV);
        velocity.clamp(-getMaxVel(), getMaxVel());
    }

    /**
     * @return The maximum absolute velocity
     */
    public double getMaxVel() {
        return MAX_VELOCITY;
    }

    /**
     * Modifies the object velocity by the specified amount along the
     * line of the current object heading angle.
     * @param magnitude size of the change in velocity
     */
    public void accelerate(final double magnitude) {
        velocity.add(new Vector2d(magnitude * Math.cos(angle),
                magnitude * Math.sin(angle)));
    }

    /**
     * Sets the position to the specified location relative to 0,0.
     * The position is specified as a Cartesian vector, and is internally
     * converted into location in the game-space under whatever space topology
     * is currently in use.
     * @param newPos vector specifying the new position
     */
    public void setPosition(final Vector2d newPos) {
        position = new ToroidalCoordinate2D(newPos);
    }

    /**
     * @return the current position as a Cartesian vector relative to 0,0
     */
    public Vector2d getPosition() {
        return new Vector2d(position.getX(), position.getY());
    }

    /**
     * Sets the velocity to the specified Cartesian vector.
     * @param newV vector specifying the new velocity
     */
    public void setVelocity(final Vector2d newV) {
        velocity = new Vector2d(newV);
    }

    /**
     * @return the current velocity as a Cartesian vector
     */
    public Vector2d getVelocity() {
        return new Vector2d(velocity);
    }

    /**
     * Rotates the object heading by the specified angle. A positive
     * rotation is in the clockwise direction.
     * @param turnAngle change in angle, in radians
     */
    public void rotate(final double turnAngle) {
        angle = angleWraparound(angle + turnAngle);
    }

    /**
     * @return the current heading angle in radians
     */
    public double getHeading() {
        return angle;
    }

    /**
     * @return the current gravity constant
     */
    public double getGravityConstant() {
        return gravityConstant;
    }

    /**
     * Sets the gravity constant to a new value
     * @param G new gravity constant value
     */
    public void setGravityConstant(final double G) {
        gravityConstant = G;
    }

    /**
     * Applies mutual gravitational forces between this object and
     * another one. Gravitational accelerations on each object
     * are computed using the standard Newtonian inverse-square law:
     *
     *   a = GM/r^2
     *
     * where GM is Newton's gravity constant multiplied by the mass of
     * the other object, and r is the distance between the objects.
     *
     * @param other the other object
     */
    public void gravitate(Actor other) {
        // Find the spatial vector between this and the other object
        final Vector2d grav_vector = this.getPosition();
        grav_vector.sub(other.getPosition());

        // Compute the acceleration magnitudes for each object as a
        // function of gravity constant (which is sort of a proxy
        // for relative mass).
        final double r2 = grav_vector.lengthSquared();
        final double my_accel = -(other.getGravityConstant() / r2);
        final double other_accel = this.getGravityConstant() / r2;

        // Convert the gravity vector into a pure direction, and then
        // scale to produce accelerations
        grav_vector.normalize();

        final Vector2d myAccelVec = new Vector2d(grav_vector);
        myAccelVec.scale(my_accel);
        this.accelerate(myAccelVec);

        final Vector2d otherAccelVec = new Vector2d(grav_vector);
        otherAccelVec.scale(other_accel);
        other.accelerate(otherAccelVec);
    }

    /**
     * Checks for collision with another object
     * @param other the other object
     * @return true if a collision has occurred with the other object
     */
    public boolean hasCollidedWith(final Actor other) {
        return this.getBoundingBox().intersects(other.getBoundingBox());
    }

    /**
     * @return a Rectangle centred on the object position, and having a size
     * corresponding to the object size.
     */
    private Rectangle2D getBoundingBox() {
        return new Rectangle2D.Double(position.getX() - 0.5 * size.getWidth(),
                position.getY() - 0.5 * size.getHeight(),
                size.getWidth(), size.getHeight());
    }

    /**
     * Makes the object inactive.
     */
    public void destroy() {
        alive = false;
    }

    /**
     * Makes the object move towards an inactive state, or become inactive
     * if it cannot sustain any more damage.
     * @param damageTaken the damage this actor should take
     */
    public void damage(int damageTaken) {
        this.destroy();
    }

    /**
     * @return the damage caused to the other actor by a collision with this actor
     */
    public abstract int getCollisionDamage();

    /**
     * @return true if the object is no longer active
     */
    public boolean isDead() {
        return !alive;
    }

    /**
     * Utility method used to keep angles within the range 0 <= angle <= 2*PI
     * @param angle an angle that may fall outside the designated range
     * @return an angle equivalent to the input, but within the
     * range 0 <= angle <= 2*PI
     */
    private static double angleWraparound(final double angle) {
        double theta = angle;
        while (theta < -Math.PI) {
            theta += 2.0 * Math.PI;
        }
        while (theta > Math.PI) {
            theta -= 2.0 * Math.PI;
        }
        return theta;
    }

    /**
     * Used to cheaply determine which subclass of Actor has been transmitted to the server
     */
    public enum ActorType {

        NEEDLE,
        WEDGE,
        STAR,
        MISSILE;

        /**
         * Retrieve the Enumeration type from its ordinal value
         * @param index the ordinal value of a ActorType
         * @return The ActorType enum value requested (null if invalid)
         */
        public static ActorType fromInt(final int index) {
            ActorType result = null;
            for (ActorType type : ActorType.values()) {
                if (type.ordinal() == index) {
                    result = type;
                    break;
                }
            }
            return result;
        }
    }

    /**
     * Builds an actor out of a data buffer
     * @param actorID the Actor's ID
     * @param type The type of actor
     * @param buffer The buffer to build from
     * @return A new actor
     */
    public static Actor fromBuffer(ActorType type, int id, double[] buffer) {
        switch (type) {
            case NEEDLE:
                return new Spacecraft.Needle(buffer);
            case WEDGE:
                return new Spacecraft.Wedge(id, buffer);
            case STAR:
                return new Star(buffer);
            case MISSILE:
                return new Missile(id, buffer);
            default:
                throw new RuntimeException("Actor build failure");
        }
    }
}
