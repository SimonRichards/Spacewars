package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;

/**
 *
 * @author Simon
 */
public class Client implements Runnable{
    private Socket localServer;
    private Socket currentServer;
    private Collection<Socket> serverList;
    
    PrintWriter out;
    BufferedReader in;
    
    public Client(int port) throws IOException {

        System.out.println("trying to connect to local server");
        localServer = new Socket("127.0.0.1", port);
        out = new PrintWriter(localServer.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(localServer.getInputStream()));
        

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String fromServer;
        String fromUser;

        while ((fromServer = in.readLine()) != null) {
            System.out.println("Server: " + fromServer);
        }

        out.close();
        in.close();
        //stdIn.close();
    }

    
    
    @Override
    public void run() {
        
    }
}
