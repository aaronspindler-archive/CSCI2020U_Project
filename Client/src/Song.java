import javafx.scene.media.Media;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.*;


public class Song {
    private String songName, artist, composer, genre, album, flag;
    private Media data;
    private File file;

    public Song(Media data, File file){
        this.data = data;
        this.file = file;
        flag = "local";
        getMetaData();
    }

    /*
    From: http://stackoverflow.com/a/21746415
     */
    public void getMetaData(){
        try {
            InputStream input = new FileInputStream(file);
            ContentHandler handler = new DefaultHandler();
            Metadata metadata = new Metadata();
            Parser parser = new Mp3Parser();
            ParseContext parseCtx = new ParseContext();
            parser.parse(input, handler, metadata, parseCtx);
            input.close();

            songName = metadata.get("title");
            artist = metadata.get("xmpDM:artist");
            composer = metadata.get("xmpDM:composer");
            genre = metadata.get("xmpDM:genre");
            album = metadata.get("xmpDM:album");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (TikaException e) {
            e.printStackTrace();
        }
    }

    public String getSongName() {
        return songName;
    }

    public String getArtist() {
        return artist;
    }

    public String getComposer() {
        return composer;
    }

    public String getGenre() {
        return genre;
    }

    public String getAlbum() {
        return album;
    }

    public void setFlag(String status) { this.flag = status; this.artist = this.artist + "\t\t\t-" + status; }
    public String getFlag() { return flag; }

    public Media getData(){
        return data;
    }

    public String getFileName() { return this.file.getName(); }

    public String toString(){
        if((songName != null) && (artist != null)) {
            return (songName + " - " + artist);
        } else if(songName != null){
            return songName;
        } else{
            return file.toString();
        }
    }
}
