package client;

import common.Command;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Captures certain key events from an AWT container
 * and stores them as a set for polling by the Client
 * @author Simon, Daniel
 */
class InputHandler extends KeyAdapter {
    private final Map<Integer,Command> commandFromKey;
    private final EnumSet<Command> commands;
    private int selectionChange;
    private boolean upHeld = false;
    private boolean downHeld = false;

    /**
     * Maps keys to commands and instantiates containers
     */
    InputHandler() {
        commandFromKey = new HashMap<Integer,Command>(Command.values().length, 1.0f);
        commandFromKey.put(KeyEvent.VK_SPACE, Command.FIRE);
        commandFromKey.put(KeyEvent.VK_W, Command.FORWARD);
        commandFromKey.put(KeyEvent.VK_A, Command.TURN_CCW);
        commandFromKey.put(KeyEvent.VK_D, Command.TURN_CW);
        commandFromKey.put(KeyEvent.VK_R, Command.RESPAWN);
        commandFromKey.put(KeyEvent.VK_ESCAPE, Command.EXIT);
        commandFromKey.put(KeyEvent.VK_Q, Command.HYPERSPACE);
        commands = EnumSet.noneOf(Command.class);
    }

    /**
     * @return The set of all currently held down buttons
     */
    synchronized EnumSet<Command> read() {
        return commands.clone();
    }

    /**
     * Adds a key's command (if any) to the set.
     * Not to be called in user code.
     * @param k the keystroke
     */
    public synchronized void keyPressed(KeyEvent k) {
        if (!upHeld && k.getKeyCode() == KeyEvent.VK_UP){
            selectionChange = -1;
            upHeld = true;
        } else if (!downHeld && k.getKeyCode() == KeyEvent.VK_DOWN) {
            selectionChange = 1;
            downHeld = true;
        }

        if (commandFromKey.containsKey(k.getKeyCode())) {
            commands.add(commandFromKey.get(k.getKeyCode()));
        }
    }

    /**
     * Removes a key's command (if any) from the set.
     * Not to be called in user code.
     * @param k the keystroke
     */
    @Override
    public synchronized void keyReleased(KeyEvent k) {
        if (commandFromKey.containsKey(k.getKeyCode())) {
            commands.remove(commandFromKey.get(k.getKeyCode()));
        }

        if (k.getKeyCode() == KeyEvent.VK_UP){
            upHeld = false;
        } else if (k.getKeyCode() == KeyEvent.VK_DOWN) {
            downHeld = false;
        }

    }

    /**
     * Retrieves the user's server selection requests, once per press.
     * @return +1 if the user has pressed down, -1 for up and 0 otherwise
     */
    synchronized int getSelectionChange() {
        int temp = selectionChange;
        selectionChange = 0;
        return temp;
    }

}
