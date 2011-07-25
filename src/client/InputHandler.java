package client;

import common.Command;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Simon, Daniel
 */
public class InputHandler extends KeyAdapter {
    private final Map<Integer,Command> commandFromKey;
    private final EnumSet<Command> commands;

    public InputHandler() {
        commandFromKey = new HashMap<Integer,Command>(Command.values().length, 1.0f);
        commandFromKey.put(KeyEvent.VK_W, Command.FIRE);
        commandFromKey.put(KeyEvent.VK_S, Command.FORWARD);
        commandFromKey.put(KeyEvent.VK_A, Command.TURN_CCW);
        commandFromKey.put(KeyEvent.VK_D, Command.TURN_CW);
        commandFromKey.put(KeyEvent.VK_ESCAPE, Command.EXIT);
        commandFromKey.put(KeyEvent.VK_SPACE, Command.HYPERSPACE);
        commands = EnumSet.noneOf(Command.class);
    }

    public synchronized EnumSet<Command> read() {
        return commands.clone();
    }

    /**
     * Accepts a keypress event and executes the corresponding command
     * on the controlled spacecraft.
     * @param k the pressed key
     */
    public synchronized void keyPressed(KeyEvent k) {
        if (commandFromKey.containsKey(k.getKeyCode())) {
            commands.add(commandFromKey.get(k.getKeyCode()));
        }
    }

    @Override
    public void keyReleased(KeyEvent k) {
        if (commandFromKey.containsKey(k.getKeyCode())) {
            commands.remove(commandFromKey.get(k.getKeyCode()));
        }
    }




}
