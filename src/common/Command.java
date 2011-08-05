package common;

/**
 * The set of keyboard commands a client uses.
 * The set of commands used to operate a
 * spaceship is a subset of this enum.
 * @author Simon, Daniel
 */
public enum Command {
    TURN_CW,
    TURN_CCW,
    FORWARD,
    EXIT,
    FIRE,
    HYPERSPACE,
    ENTRY;

    public static Command fromInt(int index) {
        Command result = null;
        for(Command command : Command.values()){
            if(command.ordinal() == index){
                result = command;
                break;
            }
        }
        return result;
    }
}
