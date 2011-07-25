package common;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.vecmath.Vector2d;
import java.awt.geom.Rectangle2D;
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
    private static final double DEFAULT_G = 100.0;
    // Image representing the object on screen, and a graphics context
    // to draw to the image.
    private BufferedImage sprite;
    protected Graphics2D spriteGraphics;
    // Size of the object in pixels
    protected Dimension size;
    // Current position and velocity in the game-space
    private ToroidalCoordinate2D position;
    private Vector2d velocity;
    // Current heading angle in radians
    private double angle = DEFAULT_HEADING;
    // "Gravity constant" of this object. Roughly analogous to GM, where
    // G is the newtonian gravity constant and M is the object mass.
    private double gravity_constant = DEFAULT_G;
    // True if the object is still active
    private boolean alive = true;
    protected int colourInt;
    private Random rand;

    public void setAngle(double angle) {
        this.angle = angle;
    }

    /**
     * Changes the colourInt field of an actor and sets the colour of the sprite
     * graphics to colourInt. The integer is divided by 1000 to give a floating
     * point with 3 digits right of the decimal point (as these are the only
     * digits which are used by getHSBColor)
     * @param colourInt
     */
    public void setColourInt(int colourInt) {
        this.colourInt = colourInt;
        spriteGraphics.setColor(Color.getHSBColor(((float)colourInt)/1000, 0.5f, 0.5f));
    }


    Actor(String stream) { // TODO throw invalid argument error
        String[] vals = stream.split(" ");
//        System.out.println(stream);
        position = new ToroidalCoordinate2D(new Vector2d(Integer.valueOf(vals[0]), Integer.valueOf(vals[1])));
        angle = ((double)Integer.valueOf(vals[2])) / 100;
//        System.out.println(angle);
        colourInt = Integer.valueOf(vals[3]);
        sprite = new BufferedImage(SPRITE_DIM, SPRITE_DIM,
                BufferedImage.TYPE_INT_ARGB);
        spriteGraphics = sprite.createGraphics();
    }


    public String toStream(){
        StringBuilder buffer = new StringBuilder(20);
        buffer.append(getID()).append(" ")
              .append((int) position.getX()).append(" ")
              .append((int) position.getY()).append(" ")
              .append((int) (angle*100)).append(" ")
              .append(colourInt);
        return buffer.toString();
    }


    public abstract int getID();

    /**
     * Creates an object at the specified position and velocity
     * @param initPos the object position
     * @param initV the object velocity
     * @param colourInt the colour for the new object. An integer between 0-1000
     * gives the full range
     */
    public Actor(Vector2d initPos, Vector2d initV, int colourInt) {
        rand = new Random();
//        colourInt = rand.nextInt(1000);
        position = new ToroidalCoordinate2D(initPos);
        velocity = new Vector2d(initV);

        // Set up the sprite that represents the object on screen
        sprite = new BufferedImage(SPRITE_DIM, SPRITE_DIM,
                BufferedImage.TYPE_INT_ARGB);
        spriteGraphics = sprite.createGraphics();

        // Default to an object size the same size as the sprite
        size = new Dimension(SPRITE_DIM, SPRITE_DIM);
        setColourInt(colourInt);
    }


    /**
     * Renders the object sprite
     * @param g the Graphics2D object to draw to
     */
    public void draw(Graphics2D g) {
        AffineTransform trans = new AffineTransform();

        // Note ordering here: first transform you want to apply gets
        // added last in the sequence
        trans.translate(position.getX(), position.getY());
        trans.rotate(angle);
        trans.translate(-SPRITE_DIM / 2, -SPRITE_DIM / 2);  // Center the image
        g.drawRenderedImage(sprite, trans);
    }

    /**
     * Updates the position base don the current velocity.
     */
    public void stepTime() {
        position.translate(velocity); // Assumes uniform timestep
    }

    /**
     * Modifies the object velocity by some vector amount
     * @param deltaV the change in velocity
     */
    public void accelerate(Vector2d deltaV) {
        velocity.add(deltaV);
    }

    /**
     * Modifies the object velocity by the specified amount along the
     * line of the current object heading angle.
     * @param magnitude size of the change in velocity
     */
    public void accelerate(double magnitude) {
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
    public void setPosition(Vector2d newPos) {
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
    public void setVelocity(Vector2d newV) {
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
    public void rotate(double turnAngle) {
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
        return gravity_constant;
    }

    /**
     * Sets the gravity constant to a new value
     * @param G new gravity constant value
     */
    public void setGravityConstant(double G) {
        gravity_constant = G;
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
        Vector2d grav_vector = this.getPosition();
        grav_vector.sub(other.getPosition());

        // Compute the acceleration magnitudes for each object as a
        // function of gravity constant (which is sort of a proxy
        // for relative mass).
        double r2 = grav_vector.lengthSquared();
        double my_accel = -(other.getGravityConstant() / r2);
        double other_accel = this.getGravityConstant() / r2;

        // Convert the gravity vector into a pure direction, and then
        // scale to produce accelerations
        grav_vector.normalize();

        Vector2d my_accel_vector = new Vector2d(grav_vector);
        my_accel_vector.scale(my_accel);
        this.accelerate(my_accel_vector);

        Vector2d other_accel_vector = new Vector2d(grav_vector);
        other_accel_vector.scale(other_accel);
        other.accelerate(other_accel_vector);
    }

    /**
     * Checks for collision with another object
     * @param other the other object
     * @return true if a collision has occurred with the other object
     */
    public boolean hasCollidedWith(Actor other) {
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
     */
    public void damage() {
        this.destroy();
    }

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
    private static double angleWraparound(double angle) {
        double theta = angle;
        while (theta < 0.0) {
            theta += 2.0 * Math.PI;
        }
        while (theta > 2.0 * Math.PI) {
            theta -= 2.0 * Math.PI;
        }
        return theta;
    }

    enum ActorType {
        NEEDLE,
        WEDGE,
        STAR,
        MISSILE,
    }

    /**
     * Gets an actor out of a int stream
     * @param stream
     * @return
     * @throws Exception
     */
    public static Actor fromStream(String stream) throws IllegalArgumentException {
        ActorType requested = null;
        int request = 0;
        //try {
         request = Integer.valueOf(stream.split(" ")[0]);
        //} catch (Exception e) { System.err.println(e.getMessage()); System.exit(-7);}
        for (ActorType type : ActorType.values()) {
            if (request == type.ordinal()) {
                requested = type;
                break;
            }
        }

        String buildStream = stream.substring(stream.indexOf(' ') + 1);
//        System.out.println(buildStream);

        if (requested == null) {
            System.err.println("Request = " + request);
            throw new IllegalArgumentException("Invalid actor type1");
        }

        Actor out;
        switch (requested) {
            case NEEDLE:
                out = new Spacecraft.Needle(buildStream);
                break;
            case WEDGE:
                out = new Spacecraft.Wedge(buildStream);
                break;
            case STAR:
                out = new Star(buildStream);
                break;
            case MISSILE:
                out = new Missile(buildStream);
                break;

            default:
                throw new IllegalArgumentException("Invalid actor type2");
        }
        return out;
    }
}
