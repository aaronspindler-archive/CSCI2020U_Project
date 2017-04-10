import javafx.application.Application;
import javafx.beans.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.*;
import java.util.Observable;

public class Main extends Application {

    static final String baseDirectory = "clientMusic/";

    private MenuBar menuBar;
    private Menu fileMenu;
    private MenuItem exitMenuItem;
    private Button playButton, prevButton, nextButton, downloadButton, shuffleButton;
    private Slider timeSlider, volumeSlider;
    private Label timeDisplay, volumeLabel;
    static ListView<Song> songList;

    private int index = 0;
    private int shuffleIndex = 0;

    private MediaPlayer mediaPlayer;
    private Duration time;
    private Boolean isPlaying = false;
    private Boolean isAtEnd = false;
    private Boolean repeatOne = false;
    private Boolean shuffled = false;
    private Socket socket;
    private int[] shuffleOrder;

    public void start(Stage primaryStage) throws Exception{
        primaryStage.setTitle("Music Player - Client");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER_LEFT);

        menuBar = new MenuBar();
        fileMenu = new Menu("Options");

        exitMenuItem = new MenuItem("Exit");
        exitMenuItem.setAccelerator(KeyCombination.keyCombination("Ctrl+Q"));
        exitMenuItem.setOnAction(e -> System.exit(0));

        fileMenu.getItems().add(exitMenuItem);
        menuBar.getMenus().add(fileMenu);

        grid.add(menuBar,0,0,12,1);

        songList = new ListView<Song>();
        songList.setItems(DataSource.getAllSongs());
        songList.setMinWidth(470);
        songList.setMaxWidth(470);
        grid.add(songList, 0, 1, 12,1);

        //listen for change of song
        songList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Song>() {
            public void changed(ObservableValue<? extends Song> observable, Song oldValue, Song newValue) {
                if (newValue.getFlag().equals("SERVER FILE")) {     //check if song is saved on the server
                    System.out.println("downloading");
                    download();                                     //download
                    System.out.println("downloaded");

                    File temp = new File(baseDirectory + newValue.getFileName());   //create new file
                    Media m = new Media(temp.toURI().toString());
                    if (isPlaying) {
                        mediaPlayer.stop();
                        isPlaying = false;
                        isAtEnd = false;
                        playButton.setText(">");
                    }
                    mediaPlayer = new MediaPlayer(m);               //play the server file
                    newValue.setFlag("DELETE");                     //set flag to delete song once playback is halted
                    index = songList.getItems().indexOf(newValue);
                } else {
                    if (isPlaying) {
                        mediaPlayer.stop();
                        isPlaying = false;
                        isAtEnd = false;
                        playButton.setText(">");
                    }
                    mediaPlayer = new MediaPlayer(newValue.getData());      //load new song into mediaplayer
                    index = songList.getItems().indexOf(newValue);          //change index to selected song's index
                }
            }
        });

        songList.setOnKeyPressed(e -> {
            if(e.getCode() == KeyCode.ENTER){           //play song on list by pressing enter
                if(isPlaying){
                    mediaPlayer.pause();
                    isPlaying = false;
                    playButton.setText(">");
                }else{
                    play();
                }
            } else if (e.getCode() == KeyCode.LEFT) {   //rewind by pressing left arrow key
                if (time.greaterThanOrEqualTo(Duration.ZERO)) {
                    mediaPlayer.seek(time.subtract(Duration.millis(500)));
                    updateValues();
                }
            } else if (e.getCode() == KeyCode.RIGHT) {  //fast forward by pressing right arrow key
                if (time.lessThanOrEqualTo(mediaPlayer.getTotalDuration())) {
                    mediaPlayer.seek(time.add(Duration.millis(500)));
                    updateValues();
                }
            }
        });


        prevButton = new Button("<<");
        prevButton.setMinWidth(100);
        prevButton.setMaxWidth(100);
        prevButton.setOnAction(e -> prev());
        grid.add(prevButton, 0, 3, 2,1);

        playButton = new Button(">");
        playButton.setMinWidth(100);
        playButton.setMaxWidth(100);
        playButton.setOnAction(e -> play());
        grid.add(playButton,2,3,2,1);

        nextButton = new Button(">>");
        nextButton.setMinWidth(100);
        nextButton.setMaxWidth(100);
        nextButton.setOnAction(e -> next());
        grid.add(nextButton, 4, 3, 2, 1);

        shuffleButton = new Button("Shuffle");
        shuffleButton.setMinWidth(100);
        shuffleButton.setMaxWidth(100);
        shuffleButton.setOnAction(e -> {            //pressing this button will shuffle the playback order
            if (!shuffled) {
                shuffle();
            } else {
                shuffled = false;
            }
        });
        grid.add(shuffleButton, 0, 4, 2, 1);

        Button repeatButton = new Button("Repeat1");
        repeatButton.setMinWidth(100);
        repeatButton.setMaxWidth(100);
        repeatButton.setOnAction(e -> { repeat(); });       //pressing this button will make the player repeat the
                                                            // selected song until toggled off
        grid.add(repeatButton, 2, 4, 2, 1);

        Button uploadButton = new Button("Upload/Download");
        uploadButton.setMinWidth(150);
        uploadButton.setMaxWidth(150);
        uploadButton.setOnAction(e -> {
            if (songList.getSelectionModel().getSelectedItem().getFlag().equals("local")) {
                upload();
            } else if (songList.getSelectionModel().getSelectedItem().getFlag().equals("SERVER FILE")) {
                songList.getSelectionModel().getSelectedItem().setFlag("local");
                download();
            }
            songList.refresh();
        });
        grid.add(uploadButton, 4,4,2,1);

        timeSlider = new Slider();
        timeSlider.setMinWidth(470);
        timeSlider.setMaxWidth(470);
        grid.add(timeSlider, 0,2,12,1);

        timeDisplay = new Label("0:00 / 0:00");
        timeDisplay.setMinWidth(470);
        timeDisplay.setMaxWidth(470);
        timeDisplay.setAlignment(Pos.CENTER);
        timeDisplay.setMouseTransparent(true);
        grid.add(timeDisplay, 0,2,12,1);

        volumeLabel = new Label("Volume: ");
        grid.add(volumeLabel, 0, 5, 6, 1);

        volumeSlider = new Slider();
        volumeSlider.setValue(1.0);
        volumeSlider.setMax(1.0);
        volumeSlider.setMin(0.0);
        volumeSlider.setMinWidth(100);
        volumeSlider.setMaxWidth(100);
        grid.add(volumeSlider, 6, 5, 6, 1);

        //listens for the user changing the position of the volume slider
        volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                mediaPlayer.setVolume(volumeSlider.getValue());
            }
        });

        Media song = DataSource.getAllSongs().get(0).getData();     //load first song
        songList.getSelectionModel().select(0);
        songList.getFocusModel().focus(0);
        mediaPlayer = new MediaPlayer(song);
        mediaPlayer.stop();

        BackgroundUpdater backgroundUpdater = new BackgroundUpdater();
        Thread backgroundUpdaterThread = new Thread(backgroundUpdater);
        backgroundUpdaterThread.start();

        Scene scene = new Scene(grid, 470,400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void prev(){
        checkFlag();                    //check if file is set to be deleted from local directory
        mediaPlayer.stop();
        if(!shuffled) {
            if (index == 0) {
                songList.getSelectionModel().select(songList.getItems().size() - 1);   //selection wrapping
            } else {
                songList.getSelectionModel().selectPrevious();
            }
        } else {
            shuffleIndex -= 1;                          //if shuffled go to the next song in shuffled order
            if (shuffleIndex <= 0) {
                shuffleIndex = shuffleOrder.length-1;
            }
            songList.getSelectionModel().select(shuffleOrder[shuffleIndex]);
        }
        play();
    }

    public void next(){
        checkFlag();
        mediaPlayer.stop();
        if (!shuffled) {
            if (index == songList.getItems().size() - 1) {
                songList.getSelectionModel().select(0);     //selection wrapping
            } else {
                songList.getSelectionModel().selectNext();
            }
        } else {
            shuffleIndex += 1;                          //if shuffled go to the next song in shuffled order
            if (shuffleIndex <= shuffleOrder.length) {
                if (shuffleIndex == shuffleOrder.length-1) {
                    shuffleIndex = 0;
                }
                songList.getSelectionModel().select(shuffleOrder[shuffleIndex]);
            }
        }
        play();
    }

    public void upload() {
        try {
            socket = new Socket("localhost", 8080);
            PrintWriter pw = new PrintWriter(socket.getOutputStream());
            pw.write("UPLOAD /" + songList.getSelectionModel().getSelectedItem().getFileName()
                    + " HTTP/1.1\r\n");  //send upload request
            pw.flush();

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            System.out.println("Uploading...");
            File localFile = new File(baseDirectory, songList.getSelectionModel().getSelectedItem().getFileName());
            byte[] contents = readFileContents(localFile);         //upload file as bytes
            out.write(contents);
            out.flush();
            System.out.println("File uploaded.");
            songList.getSelectionModel().getSelectedItem().setFlag("SERVER FILE");
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void download(){
        try {
            socket = new Socket("localhost", 8080);
            PrintWriter pw = new PrintWriter(socket.getOutputStream());
            pw.write("DOWNLOAD /" + songList.getSelectionModel().getSelectedItem().getFileName()
                    + " HTTP/1.1\r\n");         //send request for song download
            pw.flush();

            InputStream in = socket.getInputStream();
            FileOutputStream fos = new FileOutputStream((new File(baseDirectory,
                    songList.getSelectionModel().getSelectedItem().getFileName())));  //create new remote file

            int length = -1;
            byte[] buffer = new byte[1024];
            while ((length = in.read(buffer)) > -1) {
                fos.write(buffer, 0, length);           //save uploaded file data
            }

            fos.close();
            in.close();
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
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

    public void repeat() { repeatOne = !repeatOne; }        //toggle repeat

    public void shuffle(){
        shuffled = true;
        Random rng = new Random();
        Set<Integer> rand = new LinkedHashSet<>();          //set for random numbers to prevent repeats
        while (rand.size() < songList.getItems().size()-1) {
            Integer r = rng.nextInt(songList.getItems().size()-1);      //generate number
            rand.add(r);
        }
        shuffleOrder = new int[rand.size()];
        int ind = 0;
        for (Iterator i = rand.iterator(); i.hasNext();) {
            shuffleOrder[ind++] = (int) i.next();                              //save shuffle order
        }
    }

    public void checkFlag() {
        if (songList.getSelectionModel().getSelectedItem().getFlag().equals("DELETE")) {   //check if file is set
            File del = new File(baseDirectory +                                  //to be deleted
                    songList.getSelectionModel().getSelectedItem().getFileName());
            del.delete();
            songList.getSelectionModel().getSelectedItem().setFlag("SERVER FILE");         //reset flag
        }
    }

    public void play() {
        //wait for player to be ready
        mediaPlayer.setOnReady(new Runnable() {
            @Override
            public void run() {
                timeSlider.setMax(1.0);
                timeSlider.setMin(0.0);
                timeSlider.setValue(0.0);
            }
        });

        //set the player in paused status
        mediaPlayer.setOnPaused(new Runnable() {
            @Override
            public void run() {
                playButton.setText(">");
                isPlaying = false;
            }
        });

        //set the player in playing status
        mediaPlayer.setOnPlaying(new Runnable() {
            @Override
            public void run() {
                playButton.setText("||");
                if (isAtEnd) {
                    mediaPlayer.seek(time.multiply(timeSlider.getValue()/100));
                    isAtEnd = false;
                }
                isPlaying = true;
            }
        });

        //set the player in end status and shift to next song or repeat
        mediaPlayer.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                if (!repeatOne) {
                    next();
                } else {
                    mediaPlayer.seek(Duration.ZERO);
                }
            }
        });

        //update time/time slider/time display
        mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable,
                                        Duration oldValue, Duration newValue) {
                updateValues();
            }
        });

        if (!isPlaying) {
            mediaPlayer.play();
        } else {
            mediaPlayer.pause();
        }
    }

    public void updateValues() {
        time = mediaPlayer.getCurrentTime();                                                    //update time
        timeSlider.setValue(time.toSeconds() / mediaPlayer.getTotalDuration().toSeconds());     //update slider
        timeDisplay.setText(getFormattedTime(mediaPlayer.getCurrentTime()) +                    //update time display
                " / " + getFormattedTime(mediaPlayer.getTotalDuration()));
    }

    public String getFormattedTime(Duration d){
        int minutes = (int)d.toSeconds()/60;
        int seconds = (int)d.toSeconds()%60;

        return (String.format("%d:%02d", minutes, seconds));
    }

    public static void main(String[] args){
        launch(args);
    }
}