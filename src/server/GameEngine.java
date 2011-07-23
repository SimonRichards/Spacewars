package server;

import common.Actor;
import common.Spacecraft;
import common.Star;
import java.awt.Dimension;
import java.util.ArrayList;
import javax.vecmath.Vector2d;

/**
 *
 * @author Simon
 */
public class GameEngine {
    private ArrayList<Actor> actors;

    /**
     * 
     */
    public GameEngine() {
        // Central star
        Dimension size = new Dimension(50,50); //TODO: something
        this.addActor(new Star(new Vector2d(0.5*size.width, 0.5*size.height),1000.0));
     
        // The initial spacecraft
        Spacecraft s1 = new Spacecraft.Wedge();
        s1.setPosition(new Vector2d(20.0, 20.0));
        s1.setVelocity(new Vector2d(-1.0, 0));    
        this.addActor(s1);
        

        Spacecraft s2 = new Spacecraft.Needle();
        s2.setPosition(new Vector2d(400.0, 400.0));
        s2.setVelocity(new Vector2d(1.0, -1.0));
        this.addActor(s2);  
    }
    
    /**
     * 
     * @param actor
     */
    public void addActor(Actor actor) {
        actors.add(actor);
    }
        
    /**
     * Move the game state forward by one time-step. The state
     * update checks for collisions generated in the previous
     * step, applies gravitational forces between all objects, and
     * removes any objects that have somehow become dead.
     */
    public void stepTime() {
        // Look at every pair of objects to apply mutual forces 
        // and detect collisions. 
        for (int i = 0; i < actors.size(); ++i) {
            Actor actor = actors.get(i);
            for (int j = i+1; j < actors.size(); ++j) {
                Actor otherActor = actors.get(j);
                actor.gravitate(otherActor);                
                if (actor.hasCollidedWith(otherActor)) {
                    actor.damage();
                    otherActor.damage();                   
                }
            }
        } 
        
        // Update positions and mark all dead objects
        ArrayList<Actor> deadActors = new ArrayList<Actor>(actors.size());
        for (Actor obj : actors) {
            obj.stepTime();
            if (obj.isDead()) {
                deadActors.add(obj);
            }
        }
        
        // Remove dead objects
        for (Actor obj : deadActors) {
            actors.remove(obj);
        }
    }
}
