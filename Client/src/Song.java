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
    private String songName;
    private String artist;
    private Media data;
    private File file;

    public Song(Media data, File file){
        this.data = data;
        this.file = file;
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

//            Retrieve the necessary info from metadata
//            Names - title, xmpDM:artist etc. - mentioned below may differ based
//            System.out.println("----------------------------------------------");
//            System.out.println("Title: " + metadata.get("title"));
//            System.out.println("Artists: " + metadata.get("xmpDM:artist"));
//            System.out.println("Composer : "+metadata.get("xmpDM:composer"));
//            System.out.println("Genre : "+metadata.get("xmpDM:genre"));
//            System.out.println("Album : "+metadata.get("xmpDM:album"));

            songName = metadata.get("title");
            artist = metadata.get("xmpDM:artist");

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

    public Media getData() {
        return data;
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
