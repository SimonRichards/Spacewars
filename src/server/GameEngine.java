package server;

import common.Spacecraft;
import common.Star;
import common.Actor;
import common.Command;
import common.Game;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Random;
import javax.vecmath.Vector2d;

//        int milliseconds_per_frame = (int)(1000.0/FRAME_RATE);
//        timer = new Timer(milliseconds_per_frame, (ActionListener)this);
//        timer.start();

/**
 *
 * @author Simon, Daniel
 */
public class GameEngine {
    ArrayList<Actor> actors;
    AI aiActor;

    private Random rand;

    /**
     *
     */
    public GameEngine() {
        // Central star
        rand = new Random();
        actors = new ArrayList<Actor>(100);
        Dimension size = new Dimension(50,50); //TODO: something
        actors.add(new Star(new Vector2d(0.5*Game.appSize.width,
                0.5*Game.appSize.height),1000.0, 0)); //TODO: let star colour be set properly.

        // The initial spacecraft
        Spacecraft s1 = new Spacecraft.Wedge(new Vector2d(20.0, 20.0),
                new Vector2d(-1.0, 0), rand.nextInt()*1000);
        actors.add(s1);


        aiActor = new AI(new Vector2d(400.0, 400.0),
                new Vector2d(1.0, -1.0));
        actors.add(aiActor);
    }

    /**
     *
     * @param colourInt The colour (hue) of the ship, 0-1000
     * @return actor
     */
    public Spacecraft addSpaceship(int colourInt) {
        Vector2d position = new Vector2d(100,100); //TODO: get pos and vel from client, and angle
        Vector2d velocity = new Vector2d(1,-1);
        Spacecraft newActor = new Spacecraft.Wedge(position, velocity, colourInt);
        actors.add(newActor);
        return newActor;
    }

    public void act(Command command){

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
