package server;

import common.Actor;
import common.Command;
import common.Spacecraft;
import java.util.Collection;
import java.util.EnumSet;
import javax.vecmath.Vector2d;

/**
 * Unfinished
 * @author Simon, Daniel
 */
public class AI extends Spacecraft.Needle {

    private static final double AIMING_THRESH = 0.3;
    private static final double TURNING_DEADZONE = 0.1;
    private static final double AI_MAX_VELOCITY = 2;
    private static final double SLOWDOWN_ANGLE_THRESH = Math.PI / 6;
    private static final double PROXIMITY_THRESH = 100;
    private Vector2d distanceToThreat = new Vector2d();

    /**
     * Creates an AI spacecraft which is a Needle with the ability to move itself
     * @param pos The initial position
     * @param vel The initial velocity
     */
    public AI(Vector2d pos, Vector2d vel) {
        super(pos, vel);
    }

    /**
     * Tracks a player controlled spacecraft and fires if facing (roughly) towards it
     * //TODO: support multiple wedges
     * @param actors The actor list to scan for players
     * @return An appropriate command set
     */
    public Collection<Command> update(Collection<Actor> actors) {
        Collection<Command> commands = EnumSet.noneOf(Command.class);
        Command avoidCommand = avoidCollisions(actors);
        if (avoidCommand != null) {
            commands.add(avoidCommand);
        }
        if (commands.isEmpty()) {
            if (velocity.length() > AI_MAX_VELOCITY) {
                commands.add(slowDown());
            } else {
                commands = engage(actors);
            }
        }
        return commands;
    }

    /**
     * Checks all stars and wedges from the list to see whether they need to be
     * avoided and
     * @param actors The list of actors which will be avoided
     * @return A collection of dodging commands or null if
     */
    private Command avoidCollisions(Collection<Actor> actors) {
        for (Actor threat : actors) {
            if ((threat.getActorType() == Actor.ActorType.STAR.ordinal()
                    || (threat.getActorType() == Actor.ActorType.WEDGE.ordinal()))
                    && collisionImminent(threat)) {
                return dodge();
            }
        }
        return null;
    }

    /**
     * Decides whether a potential threat might collide with the ai
     * @param threat The actor which could be a threat
     * @return If the ai is in danger from the threat
     */
    private boolean collisionImminent(Actor threat) {
        distanceToThreat = new Vector2d();
        distanceToThreat.sub(threat.getPosition(), getPosition());
        if (distanceToThreat.length() < PROXIMITY_THRESH) {
            return true;
        }
        return false;
    }

    /**
     * Causes the ai to move away from a threat
     * @return A movement command
     */
    private Command dodge() {
        double angleFromCrash = Actor.angleWraparound(angle - Math.atan2(distanceToThreat.y, distanceToThreat.x)); //ai.getVelocity().angle(new Vector2d(1, 0)));
        if (angleFromCrash > 0) {
            if (angleFromCrash < Math.PI / 2) {
                return Command.TURN_CW;
            } else {
                return Command.FORWARD;
            }
        } else {
            if (angleFromCrash > -Math.PI / 2) {
                return Command.TURN_CCW;
            } else {
                return Command.FORWARD;
            }
        }
    }

    /**
     * Slows down the ai by first turning away from the direction of travel and
     * then accelerating
     * @return The movement command to effect the slowdown
     */
    private Command slowDown() {
        double angleFromTrajectory = angleWraparound(angle - Math.atan2(velocity.y, velocity.x));
        if (angleFromTrajectory > 0) {
            if (angleFromTrajectory < Math.PI - SLOWDOWN_ANGLE_THRESH) {
                return Command.TURN_CW;
            } else {
                return Command.FORWARD;
            }
        } else {
            if (angleFromTrajectory > -Math.PI + SLOWDOWN_ANGLE_THRESH) {
                return Command.TURN_CCW;
            } else {
                return Command.FORWARD;
            }
        }
    }

    /**
     * Engages with any available wedges
     * @param actors Actors, which might include wedges
     * @return Shooting and/or turning commands
     */
    private Collection<Command> engage(Collection<Actor> actors) {
        Collection<Command> commands = EnumSet.noneOf(Command.class);
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
        return commands;
    }
}
