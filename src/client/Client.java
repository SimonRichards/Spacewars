package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import server.Server;

/**
 *
 * @author Simon
 */
public class Client implements Runnable{
    private Server localServer;
    
    public Client(int port) {
        //this.localServer = localServer;
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;

        System.out.println("trying to connect to local server");
        try {
            socket = new Socket("192.168.1.100", port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: localhost.");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to localhost.");
            System.exit(1);
        }

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String fromServer;
        String fromUser;
/*
        while ((fromServer = in.readLine()) != null) {
            System.out.println("Server: " + fromServer);
            if (fromServer.equals("Bye."))
                break;
		    
            fromUser = stdIn.readLine();
	    if (fromUser != null) {
                System.out.println("Client: " + fromUser);
                out.println(fromUser);
	    }
        }
/*
        out.close();
        in.close();
        //stdIn.close();
        kkSocket.close();*/
    }

    
    
    @Override
    public void run() {
        
    }
    
    
}
