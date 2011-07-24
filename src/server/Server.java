package server;

import client.Client;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Simon
 */
public class Server implements Runnable {

    private static int DEFAULT_PORT = 2001;
    private ServerSocket serverSocket;

    private static void print(Object o) {
        System.out.println(o);
    }

    /**
     * Creates a new server on the specified port
     * @param port Socket number to use
     * @throws IOException if the Server cannot be created
     */
    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    /**
     * Main loop for the server
     */
    @Override
    public void run() {
        PrintWriter out = null;
        try {
            //address = serverSocket.getInetAddress();
            print("waiting for client");
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }
            print("client connected");
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                    clientSocket.getInputStream()));
            String inputLine, outputLine;
            //outputLine = kkp.processInput(null);
            //out.println(outputLine);
            while ((inputLine = in.readLine()) != null) {
                out.println("thing");
                System.out.println(inputLine);
                if (inputLine.equals("Bye.")) {
                    break;
                }
            }

            out.close();
            in.close();
            clientSocket.close();
            serverSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            out.close();
        }
    }

    /**
     * Program entry point
     * @param args 
     */
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        try {
            if (args.length > 0) {
                port = Integer.decode(args[0]);
            }
        } catch (NumberFormatException e) {
            System.err.println("Please enter a valid integer");
            System.exit(-1);
        }
        try {
            new Thread(new Server(port)).start();
        } catch (IOException e) {
            switch (args.length) {
                case 0:
                    System.err.println(e.getMessage() + "\nCould not bind to default port: 1989. Please try specifying one");
                    System.exit(-1);
                    break;
                default:
                    System.err.println("Could not bind to specified port");
                    System.exit(-1);
                    break;
            }
        }
        try {
            new Thread(new Client(port)).start();
        } catch (IOException ex) {
            System.err.println("Could not connect to local server");
        }

    }
}
