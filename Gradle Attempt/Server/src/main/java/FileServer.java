package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServer implements Runnable{
    int port;
    private ServerSocket serverSocket;
    public FileServer(int port){
        this.port = port;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run(){
        System.out.println("Server listening on port: " + port);

        while(true){
            Socket socket = null;
            try{
                socket = serverSocket.accept();                                         //accept new connection
                System.out.println("New connection: " + socket.toString());
                ConnectionHandler handler = new ConnectionHandler(socket);  //open new handler
                Thread handlerThread = new Thread(handler);                             //create thread for new handler
                handlerThread.start();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}
