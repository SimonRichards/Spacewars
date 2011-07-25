package server;

import common.Actor;
import common.Command;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Random;

/**
 *
 * @author Simon
 */
public class AI {
    private final Collection<Actor> actors;
    private final EnumSet<Command> output;
    private final EnumSet<Command> choices;
    private final Random rand;
    private final static int MAX_COUNT = 100;
    private final static double CHOICE_PROB = 0.5;
    private int counter = 0;
    private int counterLength = 0;


    public AI(Collection<Actor> actors) {
        this.actors = actors;
        output = EnumSet.noneOf(Command.class);
        choices = EnumSet.of(
                Command.FORWARD,
                Command.FIRE,
                Command.TURN_CCW,
                Command.TURN_CW);
        rand = new Random();
        counterLength = rand.nextInt(MAX_COUNT);
    }

    public EnumSet<Command> update(){
        if (counter++ > counterLength) {
            counterLength = rand.nextInt(MAX_COUNT);
            counter = 0;
            output.clear();
            for (Command choice : choices) {
                if (rand.nextDouble() < CHOICE_PROB) {
                    output.add(choice);
                }
            }
        }
        return output;
    }

}
