package server;

import java.util.ArrayList;
import java.util.Timer;

/**
 *
 * @author Simon
 */
public class GameEngine {
    private Timer timer;             // Game update timer
    /**
     * Move the game state forward by one time-step. The state
     * update checks for collisions generated in the previous
     * step, applies gravitational forces between all objects, and
     * removes any objects that have somehow become dead.
     */
    public void stepTime() {
        // Look at every pair of objects to apply mutual forces 
        // and detect collisions. 
        for (int i = 0; i < objects.size(); ++i) {
            SpacewarObject obj = objects.get(i);
            for (int j = i+1; j < objects.size(); ++j) {
                SpacewarObject other = objects.get(j);
                obj.gravitate(other);                
                if (obj.hasCollidedWith(other)) {
                    obj.damage();
                    other.damage();                   
                }
            }
        } 
        
        // Update positions and mark all dead objects
        ArrayList<SpacewarObject> deadObjects = new ArrayList<SpacewarObject>();
        for (SpacewarObject obj : objects) {
            obj.stepTime();
            if (obj.isDead()) {
                deadObjects.add(obj);
            }
        }
        
        // Remove dead objects
        for (SpacewarObject obj : deadObjects) {
            objects.remove(obj);
        }
    }
}
