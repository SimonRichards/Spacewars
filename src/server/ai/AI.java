package server.ai;

import common.Actor;
import common.Command;
import common.Spacecraft;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Random;
import javax.vecmath.Vector2d;

/**
 * Unfinished
 * @author Simon, Daniel
 */
public class AI extends Spacecraft.Needle {
    private final EnumSet<Command> output;
    private final EnumSet<Command> choices;
    private final Random rand;
    private final static int MAX_COUNT = 100;
    private final static double CHOICE_PROB = 0.5;
    private int counter = 0;
    private int counterLength = 0;
    private final CollisionAvoidance collisionAvoidance;
    private static final double AIMING_THRESH = 0.1;


    public AI(Vector2d pos, Vector2d vel) {
        super(pos, vel);
        output = EnumSet.noneOf(Command.class);
        choices = EnumSet.of(
                Command.FORWARD,
                Command.FIRE,
                Command.TURN_CCW,
                Command.TURN_CW);
        rand = new Random();
        counterLength = rand.nextInt(MAX_COUNT);
        collisionAvoidance = new CollisionAvoidance(this);
    }

    public Collection<Command> update(Collection<Actor> actors){ //TODO: support multiple wedges
        Collection<Command> commands = collisionAvoidance.update(actors);
        if(collisionAvoidance.isIdle()){
            for(Actor target : actors){
                if(target.getID() == Actor.ActorType.WEDGE.ordinal()){
                    double angleDiff = getHeading() - getPosition().angle(target.getPosition());
                    commands.add(angleDiff > 0 ? Command.TURN_CCW : Command.TURN_CW);
                    if(Math.abs(angleDiff) < AIMING_THRESH){
                        commands.add(Command.FIRE);
                        // TODO: might add random rotation here
                    }
                    break;
                }
            }
        }

//        if (counter++ > counterLength) {
//            counterLength = rand.nextInt(MAX_COUNT);
//            counter = 0;
//            output.clear();
//            for (Command choice : choices) {
//                if (rand.nextDouble() < CHOICE_PROB) {
//                    output.add(choice);
//                }
//            }
//        }
//        return output;
        return commands;
    }
}
