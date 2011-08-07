package server;

import common.Spacecraft;
import common.Star;
import common.Actor;
import common.Game;
import java.util.ArrayList;
import javax.vecmath.Vector2d;

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
    private final static int AI_RESPAWN_PERIOD = 100;
    private int aiRespawnCounter = AI_RESPAWN_PERIOD;

    /**
     * Adds the default actors to a new GameEngine
     */
    GameEngine() {
        actors = new ArrayList<Actor>(Game.POPCAP); //NB: Pop cap not actually enforced

        // Add the star(s)
        Vector2d starPos = new Vector2d(0.25 * Game.APPSIZE.width * (1 + 2 * Game.rand.nextDouble()),
                0.25 * Game.APPSIZE.height * (1 + 2 * Game.rand.nextDouble()));
        Star firstStar = new Star(starPos);
        actors.add(firstStar);

        // 50/50 chance of getting a binary star
        // Distances and velocities for binary stars were not found with maths, changing anything
        // Including the appsize, will necessitate disabling this feature
        double star_dist, star_vel;
        if (Game.rand.nextBoolean()) {
            // Place the left star in the left side of the screen
            double x = Game.rand.nextDouble() * Game.APPSIZE.width / 2;
            // And in the central half of the y axis
            double y = (2 * Game.rand.nextDouble() * Game.APPSIZE.height + Game.APPSIZE.height) / 4;
            firstStar.setPosition(new Vector2d(x, y));

            // 50/50 split on the binary stars' initial separation
            if (Game.rand.nextBoolean()) {
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
        addAiActor();
    }

    /**
     * Creates a new client controlled spaceship
     * @param id The clients id, which also determines the spacecraft's colour
     * @return actor The new actor (which has already been added to the actor collection
     */
    Spacecraft addSpaceship(int id) {
        Vector2d position = new Vector2d(Game.rand.nextInt(Game.APPSIZE.width), Game.rand.nextInt(Game.APPSIZE.height));
        Vector2d velocity = new Vector2d(10 * (Game.rand.nextDouble() - 0.5), 10 * (Game.rand.nextDouble() - 0.5));
        Spacecraft newActor = new Spacecraft.Wedge(id, position, velocity);
        newActor.rotate(Game.rand.nextDouble() * Math.PI * 2);
        actors.add(newActor);
        return newActor;
    }

    private void addAiActor() {
        aiActor = new AI(new Vector2d(400.0, 400.0),
                new Vector2d(1.0, -1.0));
        actors.add(aiActor);
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
            for (int j = i + 1; j < actors.size(); ++j) {
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

        if (aiActor.isDead()) {
            if (--aiRespawnCounter < 0) {
                addAiActor();
                aiRespawnCounter = AI_RESPAWN_PERIOD;
            }
        }
    }
}
