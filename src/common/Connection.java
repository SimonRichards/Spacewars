package common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Simon, Daniel
 */
public abstract class Connection {

    protected Socket socket;
    protected PrintWriter out;
    protected BufferedReader in;
    protected InputStream stream;
    protected String name;

    public void writeln(String string) {
        out.println(string);
    }

    public void writeln(int integer) {
        out.println(integer);
    }

    public String getName() {
        return name;
    }

    public void write(char c) {
        out.print(c);
    }

    public void writeln(char c) {
        out.println(c);
    }

    public abstract String readln() throws IOException;

    public boolean isAlive() {
        try {
            in.ready();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public void close() {
        out.close();
        try {
            in.close();
            socket.close();
        } catch (IOException ex) {
            System.err.println("Client connection closing failed");
        }
    }

    public static class Client extends Connection {

        public Client(Socket socket) throws IOException {
            this.socket = socket;
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            stream = socket.getInputStream();
        }

        @Override
        public String readln() throws IOException {
            String command = null;
            if(in.ready()){
                command = in.readLine();
            }
            return command;
        }

        public boolean isOnServer(){
            return true;
        }
    }

    public static class Server extends Connection {

        private int count = 0;
        private int total = 0;
        private boolean inProgress = false;

        public Server(InetAddress host, int port, String name) throws IOException {
            this.name = name;
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            stream = socket.getInputStream();
            socket.setSoTimeout(500);
        }

        public int getNumActors() throws IOException {
            return Integer.valueOf(in.readLine());
        }

        public String readln() throws IOException {
            return in.readLine();
        }


        public boolean is(InetAddress address) {
            return address.equals(socket.getInetAddress());
        }
    }
}
