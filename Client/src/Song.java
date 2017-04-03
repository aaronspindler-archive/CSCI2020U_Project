import javafx.scene.media.Media;

public class Song {
    String songName;
    String artist;
    Media data;

    public Song(Media data){
        this.data = data;
    }

    public void findSongName(){

    }

    public void findArtist(){

    }

    public String toString(){
        if((songName != null) && (artist != null)) {
            return (songName + " - " + artist);
        } else if(songName != null){
            return songName;
        } else{
            return data.getSource();
        }
    }
}
