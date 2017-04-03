import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.media.Media;

import java.io.File;

public class DataSource {
    static public ObservableList<Song> getAllSongs(){
        ObservableList<Song> songs = FXCollections.observableArrayList();
        File rootDirectory = new File(Main.baseDirectory);
        File[] files = rootDirectory.listFiles();

        for(int i = 0; i < files.length; i++){
            Media tempData = new Media(files[i].toURI().toString());
            Song tempSong = new Song(tempData, files[i]);
            songs.add(tempSong);
        }

        return songs;
    }
}
