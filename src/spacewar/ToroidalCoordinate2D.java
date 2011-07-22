package spacewar;

import javax.vecmath.Vector2d;
import java.awt.Dimension;


/**
 * Models a toroidal space for objects to move in.
 */
class ToroidalCoordinate2D {

    // Default values/
    private static final Vector2d DEFAULT_POS = new Vector2d(0.0, 0.0);
    private static final Dimension DEFAULT_DIM = new Dimension(500, 500);

    // Current location in the space
    private Vector2d pos;
    
    // Size of the space
    private Dimension dim;    

    /**
     * Constructs and initializes a point with default 
     * coordinates in a space of default size. 
     */
    public ToroidalCoordinate2D() {
        this(DEFAULT_POS, DEFAULT_DIM);
    }

    /**
     * Constructs and initializes a point with the specified coordinates
     * in a space of default size. 
     * @param pos initial coordinates
     */
    public ToroidalCoordinate2D(Vector2d pos) {
        this(pos, DEFAULT_DIM);
    }    

    /**
     * Constructs and initializes a point with default 
     * coordinates in a space of the specified size. 
     * @param dim size of the space
     */
    public ToroidalCoordinate2D(Dimension dim) {
        this(DEFAULT_POS, dim);
    }    

    /**
     * Constructs and initializes a point with the specified coordinates
     * in a space of the specified size. 
     * @param pos initial coordinates
     * @param dim size of the space     
     */
    public ToroidalCoordinate2D(Vector2d pos, Dimension dim) {
        this.pos = new Vector2d(0.0, 0.0);
        this.dim = new Dimension(dim);
        this.translate(pos); // Ensure space wraparound        
    }
    
    /** 
     * Translates this point, at location (x, y), along a vector
     * (dx, dy) so that the resulting coordinates are for the 
     * point (x + dx, y + dy). In this case the '+' operator
     * is for a toroidal space, e.g. for x + dx > size of the space
     * the result is wrapped around to fall within the range
     * 0 < x < size.
     *
     * @param delta the vector defining dx and dy
     */
    public void translate(Vector2d delta) {
        pos.add(delta);
        
        // Check and correct for x wraparound
        if (pos.x < 0) {
            pos = new Vector2d(dim.getWidth(), pos.y);
        }
        else if (pos.x > dim.getWidth()) {
            pos = new Vector2d(0.0, pos.y);
        }
        
        // Check and correct for y wraparound
        if (pos.y < 0) {
            pos = new Vector2d(pos.x, dim.getHeight());
        }    
        else if (pos.y > dim.getHeight()) {
            pos = new Vector2d(pos.x, 0.0);     
        }
    }
    
    /**
     * @return the x coordinate
     */
    public double getX() {
        return pos.x;
    }
    
    /**
     * @return the y coordinate
     */
    public double getY() {
        return pos.y;
    }    
}