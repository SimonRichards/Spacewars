package server;

import client.Client;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Simon
 */
public class Server implements Runnable{

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
    public Server(int port) throws IOException{
        serverSocket = new ServerSocket(port);
    }
    
    
    /**
     * Main loop for the server
     */
    @Override
    public void run() {
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
        /*
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(
				new InputStreamReader(
				clientSocket.getInputStream()));
        String inputLine, outputLine;*/
/*
        outputLine = kkp.processInput(null);
        out.println(outputLine);

        while ((inputLine = in.readLine()) != null) {
             outputLine = kkp.processInput(inputLine);
             out.println(outputLine);
             if (outputLine.equals("Bye."))
                break;
        }*//*
        out.close();
        in.close();
        clientSocket.close();
        serverSocket.close();*/
    }
    
    
    /**
     * Program entry point
     * @param args 
     */
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        try {
            if (args.length > 0 ) {
                port = Integer.decode(args[0]);
            }
        } catch (NumberFormatException e) {
            System.err.println("Please enter a valid integer");
            System.exit(-1);
        }
        try {
            new Thread(new Server(port)).start();
            new Thread(new Client(port)).start();
        } catch (IOException e) { 
            switch(args.length) {
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
    }
}
