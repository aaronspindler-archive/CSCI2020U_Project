package Server;

import java.util.Scanner;

public class Main {
    static final int PORT = 8080;
    public static void main(String[] args){
        Scanner input = new Scanner(System.in);

        System.out.println("=== Music Server ===");

        FileServer server = new FileServer(PORT);
        Thread serverThread = new Thread(server);
        serverThread.start();
    }
}
