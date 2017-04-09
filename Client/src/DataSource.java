import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.media.Media;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class DataSource {

    static ObservableList<Song> songs = FXCollections.observableArrayList();
    static public ObservableList<Song> getAllSongs(){
        songs = FXCollections.observableArrayList();
        File rootDirectory = new File(Main.baseDirectory);
        File[] files = rootDirectory.listFiles();

        for(int i = 0; i < files.length; i++){
            Media tempData = new Media(files[i].toURI().toString());
            Song tempSong = new Song(tempData, files[i]);
            songs.add(tempSong);
        }

        try {
            Socket socket = new Socket("localhost",8080);
            PrintWriter pw = new PrintWriter(socket.getOutputStream());
            pw.write("DIR / HTTP/1.1\r\n");                         //send DIR request
            pw.flush();

            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String file = "";
            while ((file = br.readLine()) != null) {
                File f = new File("serverMusic/" + file);
                Media tempData = new Media(f.toURI().toString());
                Song tempSong = new Song(tempData, f);
                tempSong.setFlag("SERVER FILE");
                songs.add(tempSong);
            }
            br.close();
            pw.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return songs;
    }
}
