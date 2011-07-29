package common;

import java.awt.*;
import java.nio.DoubleBuffer;
import javax.vecmath.Vector2d;

/**
 * A star is a large gravitating object that twinkles. Stars cannot be
 * damaged, destroyed, or moved.
 *
 * @author AIM, Simon, Daniel
 */
public class Star extends Actor {

    private static final double DEFAULT_G = 1000000.0;
    private static final int STAR_CRASH_EFFECT = 1000;
    private static Polygon shape = new Polygon(new int[] {10, 20, 11, 10, 9, 0, 10,
                                                          10, 9, 5, 15, 11, 5, 15},
                                               new int[] {10, 10, 11, 20, 9, 10, 10,
                                                          0, 11, 5, 15, 9, 15, 5},
                                               14);
    /**
     * Place a star at the specified location, with a default gravitational
     * constant.
     * @param pos the location of the star
     */
    public Star(Vector2d pos) {
        this(pos, DEFAULT_G);
    }

    /**
     * Rebuilds a star out of transmitted data
     * @param buffer The data
     */
    Star(double[] buffer) {
        super(0, buffer);
        setSprite();
    }

    /**
     * Place a star with gravitational constant G at the specified location.
     * @param pos the location of the star
     * @param G the gravitational constant of the star.
     */
    public Star(Vector2d pos, double G) {
        super(new Vector2d(pos), new Vector2d(1.0, 0.0));
        this.setGravityConstant(G);
        setSprite();
    }

    /**
     * Not even possible
     */
    @Override
    public void destroy() {
        return;
    }

    /**
     * Set's up the star's sprite, for use by constructors
     */
    private void setSprite() {
        // Define a sprite for the star. This is basically just a whole
        // mess of lines.
        spriteGraphics.setColor(Color.WHITE);
        spriteGraphics.drawPolygon(shape);
    }

    /**
     * This method is ignored, stars are immutable
     * @param damageTaken ignored
     */
    @Override
    public void damage(int damageTaken) {
        return;
    }

    /**
     * @inheritDoc
     * Used for animation purposes only
     */
    @Override
    public void stepTime() {
        // Even though ships are defined as having effectively zero mass,
        // we *ensure* that a star remains fixed by disabling position
        // updates. This avoids any problems with numerical rounding
        // errors. A more realistic model might allow stars to influence
        // each other. This is easily achieved by inserting a call to
        // super.stepTime() here.

        // Although stars can't move, but rotating them gives a twinkle
        this.rotate(Math.PI/4.0);
    }


    /**
     * @return ActorType.STAR's ordinal value
     */
    @Override
    public int getActorType() {
        return ActorType.STAR.ordinal();
    }

    /**
     * @return a Very Large number
     */
    @Override
    public int getCollisionDamage() {
        return STAR_CRASH_EFFECT;
    }
}

