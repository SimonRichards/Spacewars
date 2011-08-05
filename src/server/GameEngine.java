package server;

import common.Spacecraft;
import common.Star;
import common.Actor;
import common.Game;
import java.util.ArrayList;
import java.util.Random;
import javax.vecmath.Vector2d;
import server.ai.AI;


/**
 * The GameEngine provides a front end to the physics engine
 * and holds a collection of all in-game actors
 * @author AIM, Simon, Daniel
 */
class GameEngine {
    ArrayList<Actor> actors; // All the current in-game actors
    AI aiActor;              // The solitary AI spacecraft
    private final static double WIDE_STAR_VEL = 1.5;
    private final static double WIDE_STAR_DIST = 200;
    private final static double TIGHT_STAR_VEL = 2;
    private final static double TIGHT_STAR_DIST = 100;

    private Random rand;

    /**
     * Adds the default actors to a new GameEngine
     */
    GameEngine() {
        rand = new Random();
        actors = new ArrayList<Actor>(Game.popcap); //NB: Pop cap not actually enforced

        // Add the star(s)
        Vector2d starPos = new Vector2d(0.25*Game.appSize.width*(1 + 2*rand.nextDouble()),
                0.25*Game.appSize.height*(1 + 2*rand.nextDouble()));
        Star firstStar = new Star(starPos);
        actors.add(firstStar);

        // 50/50 chance of getting a binary star
        // Distances and velocities for binary stars were not found with maths, changing anything
        // Including the appsize, will necessitate disabling this feature
        double star_dist, star_vel;
        if (rand.nextBoolean()) {
            // Place the left star in the left side of the screen
            double x = rand.nextDouble()*Game.appSize.width/2;
            // And in the central half of the y axis
            double y = (2*rand.nextDouble()*Game.appSize.height + Game.appSize.height) / 4;
            firstStar.setPosition(new Vector2d(x,y));

            // 50/50 split on the binary stars' initial separation
            if (rand.nextBoolean()) {
                star_dist = WIDE_STAR_DIST;
                star_vel = WIDE_STAR_VEL;
            } else {
                star_dist = TIGHT_STAR_DIST;
                star_vel = TIGHT_STAR_VEL;
            }
            Vector2d binaryPos = new Vector2d(firstStar.getPosition());
            binaryPos.add(new Vector2d(star_dist, 0));
            Star secondStar = new Star(binaryPos);
            firstStar.setVelocity(new Vector2d(0, star_vel));
            secondStar.setVelocity(new Vector2d(0, -star_vel));
            actors.add(secondStar);
        }

        // Add the AI spacecraft
        aiActor = new AI(new Vector2d(400.0, 400.0),
                new Vector2d(1.0, -1.0));
        actors.add(aiActor);
    }

    /**
     * Creates a new client controlled spaceship
     * @param id The clients id, which also determines the spacecraft's colour
     * @return actor The new actor (which has already been added to the actor collection
     */
    Spacecraft addSpaceship(int id) {
        Vector2d position = new Vector2d(rand.nextInt(Game.appSize.width),rand.nextInt(Game.appSize.height));
        Vector2d velocity = new Vector2d(10*(rand.nextDouble()-0.5),10*(rand.nextDouble()-0.5));
        Spacecraft newActor = new Spacecraft.Wedge(id, position, velocity);
        newActor.rotate(rand.nextDouble()*Math.PI*2);
        actors.add(newActor);
        return newActor;
    }

    /**
     * Move the game state forward by one time-step. The state
     * update checks for collisions generated in the previous
     * step, applies gravitational forces between all objects, and
     * removes any objects that have somehow become dead.
     */
    void stepTime() {
        // Look at every pair of objects to apply mutual forces
        // and detect collisions.
        for (int i = 0; i < actors.size(); ++i) {
            Actor actor = actors.get(i);
            for (int j = i+1; j < actors.size(); ++j) {
                Actor otherActor = actors.get(j);
                actor.gravitate(otherActor);
                if (actor.hasCollidedWith(otherActor)) {
                    actor.damage(otherActor.getCollisionDamage());
                    otherActor.damage(actor.getCollisionDamage());
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
