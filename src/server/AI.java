package server;

import common.Actor; //TODO: for all files, fix imports
import common.Command;
import common.Spacecraft;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Random;
import javax.vecmath.Vector2d;

/**
 * Unfinished
 * @author Simon, Daniel
 */
public class AI extends Spacecraft.Needle {

    private final CollisionAvoidance collisionAvoidance;
    private static final double AIMING_THRESH = 0.3;
    private static final double TURNING_DEADZONE = 0.1;
    private static final double AI_MAX_VELOCITY = 2;
    private static final double SLOWDOWN_ANGLE_THRESH = Math.PI / 6;

    /**
     * Creates an AI spacecraft which is a Needle with the ability to move itself
     * @param pos The initial position
     * @param vel The initial velocity
     */
    public AI(Vector2d pos, Vector2d vel) {
        super(pos, vel);
        collisionAvoidance = new CollisionAvoidance(this);
    }

    /**
     * Tracks a player controlled spacecraft and fires if facing (roughly) towards it
     * @param actors The actor list to scan for players
     * @return An appropriate command set
     */
    public Collection<Command> update(Collection<Actor> actors) { //TODO: support multiple wedges
        Collection<Command> commands = collisionAvoidance.update(actors);
        if (collisionAvoidance.isIdle()) {
            if (velocity.length() > AI_MAX_VELOCITY) {
                double angleFromTrajectory = angleWraparound(angle - Math.atan2(velocity.y, velocity.x));
                System.out.println("slowing. angle: " + angle + " velAngle: " + Math.atan2(velocity.y, velocity.x) + " aFT: " + angleFromTrajectory);
                if (angleFromTrajectory > 0) {
                    if (angleFromTrajectory < Math.PI - SLOWDOWN_ANGLE_THRESH) {
                        commands.add(Command.TURN_CW);
                    } else {
//                System.out.println("forwards");
                        commands.add(Command.FORWARD);
//                commands.add(Command.TURN_CW);
                    }
                } else {
                    if (angleFromTrajectory > -Math.PI + SLOWDOWN_ANGLE_THRESH) {
                        commands.add(Command.TURN_CCW);
                    } else {
//                System.out.println("forwards");
                        commands.add(Command.FORWARD);
//                commands.add(Command.TURN_CCW);
                    }
                }
            } else {
                searchLoop:
                for (Actor target : actors) {
                    if (target.getActorType() == Actor.ActorType.WEDGE.ordinal()) {
                        double angleDiff = getHeading() - Math.atan2(target.getPosition().y - getPosition().y, target.getPosition().x - getPosition().x);
                        angleDiff %= Math.PI * 2;
                        if (angleDiff > Math.PI) {
                            angleDiff -= 2 * Math.PI;
                        }
                        if (Math.abs(angleDiff) > TURNING_DEADZONE) {
                            commands.add(angleDiff > 0 ? Command.TURN_CCW : Command.TURN_CW);
                        }
                        if (Math.abs(angleDiff) < AIMING_THRESH) {
                            commands.add(Command.FIRE);
                            break searchLoop;
                        }
                    }
                }
            }
        }
        return commands;
    }
}
