package server;

import common.Actor;
import common.Command;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import javax.vecmath.Vector2d;

/**
 * Unfinished
 * @author Simon, Daniel
 */
public class CollisionAvoidance {

    private boolean idle = true;
    private AI ai;
    private static final double PROXIMITY_THRESH = 5;
    private static final double SAFETY_FACTOR = 1.5;

    public CollisionAvoidance(AI ai){
        this.ai = ai;
    }

    public boolean isIdle() {
        return idle;
    }

    public Collection<Command> update(Collection<Actor> actors){
        idle = true;
        Collection<Command> commands = EnumSet.noneOf(Command.class);
        for(Actor threat : actors){
            if(collisionImminent(threat)){
                commands = avoid();
                idle = false;
                break;
            }
        }
        return commands;
    }

    private boolean collisionImminent(Actor threat){
        Vector2d distance = new Vector2d();
        distance.sub(threat.getPosition(), ai.getPosition());
        if(distance.length() < PROXIMITY_THRESH){
            Vector2d positionDiff = new Vector2d();
            positionDiff.sub(ai.getPosition(), threat.getPosition());
            Vector2d velocityDiff = new Vector2d();
            velocityDiff.sub(ai.getVelocity(), threat.getVelocity());
            double a = velocityDiff.dot(velocityDiff);
            double b = 2*velocityDiff.dot(positionDiff);
            double c = positionDiff.dot(positionDiff)
                    - SAFETY_FACTOR*Math.pow(ai.getSize().height + threat.getSize().height, 2);
            if(b*b - 4*a*c > 0){
                return true;
            }
        }
        return false;
    }

    private Collection<Command> avoid(){
        Collection<Command> commands = new LinkedList<Command>();
        double angleFromCrash = (ai.getHeading() - ai.getVelocity().angle(new Vector2d(1, 0)));
        if(angleFromCrash > 0){
            if(angleFromCrash < Math.PI/2){
                commands.add(Command.TURN_CCW);
            }
            else{
                commands.add(Command.FORWARD);
                commands.add(Command.TURN_CCW);
            }
        }
        else{
            if(angleFromCrash > -Math.PI/2){
                commands.add(Command.TURN_CW);
            }
            else{
                commands.add(Command.FORWARD);
                commands.add(Command.TURN_CW);
            }
        }
        return commands;
    }
}
