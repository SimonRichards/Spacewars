package server;

import client.Client;
import common.SpacewarGame;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ServerSocket;
import javax.swing.JFrame;

/**
 *
 * @author Simon
 */
public class Server implements Runnable{

    private static int DEFAULT_PORT = 1989;
    
    private static final String appName = "Spacewar(s)!";
    private static final Dimension appSize = new Dimension(500, 500);
    
    public Server(int port) throws IOException{
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println(serverSocket.getInetAddress().getCanonicalHostName());
        
        /*
        Socket clientSocket = null;
        try {
            clientSocket = serverSocket.accept();
        } catch (IOException e) {
            System.err.println("Accept failed.");
            System.exit(1);
        }

        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(
				new InputStreamReader(
				clientSocket.getInputStream()));
        String inputLine, outputLine;
        KnockKnockProtocol kkp = new KnockKnockProtocol();

        outputLine = kkp.processInput(null);
        out.println(outputLine);

        while ((inputLine = in.readLine()) != null) {
             outputLine = kkp.processInput(inputLine);
             out.println(outputLine);
             if (outputLine.equals("Bye."))
                break;
        }
        out.close();
        in.close();
        clientSocket.close();
        serverSocket.close();*/
    }
    
    
    
    @Override
    public void run() {
        
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
        int port = DEFAULT_PORT;
        try {
            if (args.length > 0 ) {
                port = Integer.decode(args[0]);
            }
        } catch (NumberFormatException e) {
                System.err.println("Please enter a valid integer");
        }
        try {
            Server server = new Server(port);
            new Thread(new Server(port)).start();
            new Thread(new Client(port, server)).start();
        } catch (IOException e) { 
            switch(args.length) {
                case 0:
                    System.err.println(e.getMessage() + "\nCould not bind to default port: 1989. Please try specifying one");
                    break;
                default:
                    System.err.println("Could not bind to specified port");
                    break;                    
            }
        }
    }
}
