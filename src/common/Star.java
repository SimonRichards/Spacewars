package common;

import java.awt.*;
import javax.vecmath.Vector2d;

/**
 * A star is a large gravitating object that twinkles. Stars cannot be
 * damaged, destroyed, or moved.
 */
public class Star extends SpacewarObject {
    
    private static final double DEFAULT_G = 1000.0;
        
    /** 
     * Place a star at the specified location, with a default gravitational
     * constant.
     * @param pos the location of the star
     */
    public Star(Vector2d pos) {
        this(pos, DEFAULT_G);
    }
    
    /** 
     * Place a star with gravitational constant G at the specified location.
     * @param pos the location of the star
     * @param G the gravitational constant of the star.
     */    
    public Star(Vector2d pos, double G) {
        super(new Vector2d(pos), new Vector2d(1.0, 0.0));
        this.setGravityConstant(G);
        
        // Define a sprite for the star. This is basically just a whole 
        // mess of lines.
        Polygon shape = new Polygon(new int[] {10, 20, 11, 10, 9, 0, 10, 
                                               10, 9, 5, 15, 11, 5, 15},
                                    new int[] {10, 10, 11, 20, 9, 10, 10, 
                                               0, 11, 5, 15, 9, 15, 5},
                                    14);                               
        spriteGraphics.setColor(Color.WHITE);
        spriteGraphics.drawPolygon(shape);
    }

    @Override
    public void destroy() {
        return;
    }
    
    @Override
    public void damage() {
        return;
    }
    
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
}

