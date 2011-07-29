package common;

/**
 *
 * @author Simon, Daniel
 */
public enum Command {
    TURN_CW,
    TURN_CCW,
    FORWARD,
    EXIT,
    FIRE,
    HYPERSPACE,
    RESPAWN,
    ENTRY;

    public static Command fromInt(int a) {
        Command command = null;
        for(Command c : Command.values()){
            if(c.ordinal() == a){
                command = c;
            }
        }
        return command;
    }
}
