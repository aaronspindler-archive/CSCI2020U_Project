package Server;

import java.io.*;
import java.net.Socket;

public class ConnectionHandler implements Runnable{
    private Socket socket;
    private DataOutputStream out;
    private final static String WEB_ROOT = "serverMusic";
    private File remote = new File(WEB_ROOT);           //remote folder

    public ConnectionHandler(Socket socket){
        this.socket = socket;
    }
    public void run() {
        try{
            while(true){
                InputStream is = socket.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(is));
                OutputStream os = socket.getOutputStream();
                out = new DataOutputStream(os);
                out.flush();

                String request = in.readLine();                     //read request
                String[] requestParts = request.split(" ");
                String command = requestParts[0];
                String fileName = requestParts[1];

                System.out.println(command);

                if (command.equalsIgnoreCase("DIR")) {              //respond to DIR commands
                    sendDirectory();
                    socket.close();
                } else if (command.equalsIgnoreCase("UPLOAD")) {    //respond to UPLOAD requests
                    uploadFile(fileName);
                    socket.close();
                } else if (command.equalsIgnoreCase("DOWNLOAD")) {  //respond to DOWNLOAD requests
                    sendFile(fileName);
                    socket.close();
                }
            }
        }catch(Exception e){}
    }

    public void uploadFile(String fileName) {
        try {
            InputStream in = socket.getInputStream();
            FileOutputStream fos = new FileOutputStream((new File(WEB_ROOT,fileName)));  //create new remote file

            int length = -1;
            byte[] buffer = new byte[1024];
            while ((length = in.read(buffer)) > -1) {
                fos.write(buffer, 0, length);           //save uploaded file data
            }

            fos.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendFile(String fileName) {
        File localDownload = new File(WEB_ROOT, fileName);      //file to be send to client
        if (localDownload.exists()) {
            byte[] contents = readFileContents(localDownload);  //get file byte array
            sendResponse(contents);                             //send file
        }
    }

    private byte[] readFileContents(File file) {
        byte[] content = new byte[(int) file.length()];
        try {
            FileInputStream fis = new FileInputStream(file);
            fis.read(content);                                  //read file into byte array
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }

    private void sendResponse(byte[] content) {
        try {
            byte[] end = "\r\n".getBytes();
            out.write(content);                     //respond with requested content
            out.write(end);                         //end response
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendDirectory() {
        File[] ls = remote.listFiles();             //get list of server files
        byte[] content;
        for (int i = 0; i < ls.length; i++) {
            content = ls[i].getName().getBytes();   //add file names to list
            sendResponse(content);                  //send
        }
    }
}
