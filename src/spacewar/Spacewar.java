/**
 * A simple version of the classic Spacewar! game.
 */
package spacewar;

import javax.swing.*;        
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Spacewar {
    private static final String appName = "Spacewar!";
    private static final Dimension appSize = new Dimension(500, 500);
    
    private Spacewar(){};
    
    /**
     * Create the GUI and show it. For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        // Create and set up the window.
        JFrame frame = new JFrame(appName);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Specify the size
        frame.setSize(appSize);
    
        // Add the game (make it the same size as the window
        // in this case).
        final Canvas game = new SpacewarGame(appSize);
        frame.getContentPane().add(game);

        // Make the game get the focus when the frame is activated.
        frame.addWindowFocusListener(new WindowAdapter() {
            public void windowGainedFocus(WindowEvent e) {
                game.requestFocusInWindow();
            }
        });

        // Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
