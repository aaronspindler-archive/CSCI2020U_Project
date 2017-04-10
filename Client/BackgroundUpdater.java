import javafx.application.Platform;
import javafx.scene.media.Media;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public class BackgroundUpdater implements Runnable{

    /*
    From: http://andreinc.net/2013/12/06/java-7-nio-2-tutorial-writing-a-simple-filefolder-monitor-using-the-watch-service-api/
     */
    public static void watchDirectoryPath(Path path) {
        // Sanity check - Check if path is a folder
        try {
            Boolean isFolder = (Boolean) Files.getAttribute(path,
                    "basic:isDirectory", NOFOLLOW_LINKS);
            if (!isFolder) {
                throw new IllegalArgumentException("Path: " + path + " is not a folder");
            }
        } catch (IOException ioe) {
            // Folder does not exists
            ioe.printStackTrace();
        }

        System.out.println("Watching path: " + path);

        // We obtain the file system of the Path
        FileSystem fs = path.getFileSystem ();

        // We create the new WatchService using the new try() block
        try(WatchService service = fs.newWatchService()) {

            // We register the path to the service
            // We watch for creation events
            path.register(service, ENTRY_CREATE);

            // Start the infinite polling loop
            WatchKey key = null;
            while(true) {
                key = service.take();

                // Dequeueing events
                Kind<?> kind = null;
                for(WatchEvent<?> watchEvent : key.pollEvents()) {
                    // Get the type of the event
                    kind = watchEvent.kind();
                    if (OVERFLOW == kind) {
                        continue; //loop
                    } else if (ENTRY_CREATE == kind) {
                        // A new Path was created
                        Path newPath = ((WatchEvent<Path>) watchEvent).context();
                        // Output
                        System.out.println("New path created: " + newPath);
                        File tempFile = new File(Main.baseDirectory + newPath);
                        Media tempData = new Media(tempFile.toURI().toString());
                        Song tempSong = new Song(tempData, tempFile);
                        DataSource.songs.add(tempSong);

                        Platform.runLater(
                                () -> {
                                    Main.songList.setItems(DataSource.songs);
                                }
                        );
                    }
                }

                if(!key.reset()) {
                    break; //loop
                }
            }

        } catch(IOException ioe) {
            //ioe.printStackTrace();
        } catch(InterruptedException ie) {
            //ie.printStackTrace();
        }

    }

    public void run() {
        Path folder = Paths.get(Main.baseDirectory);
        watchDirectoryPath(folder);
    }
}
