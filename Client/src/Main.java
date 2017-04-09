import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

public class Main extends Application {

    static final String baseDirectory = "clientMusic/";

    MenuBar menuBar;
    Menu fileMenu;
    MenuItem exitMenuItem;
    Button playButton, prevButton, nextButton, downloadButton, shuffleButton;
    Slider timeSlider, volumeSlider;
    Label timeDisplay, volumeLabel;
    ListView<Song> songList;

    int index = 0;
    int shuffleIndex = 0;

    MediaPlayer mediaPlayer;
    Duration time;
    Boolean isPlaying = false;
    Boolean isAtEnd = false;
    Boolean repeatOne = false;
    Boolean shuffled = false;
    int[] shuffleOrder;

    public void start(Stage primaryStage) throws Exception{
        primaryStage.setTitle("Music Player - Client");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_LEFT);

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

        songList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Song>() {
            public void changed(ObservableValue<? extends Song> observable, Song oldValue, Song newValue) {
                if(isPlaying){
                    mediaPlayer.stop();
                    isPlaying = false;
                    isAtEnd = false;
                    playButton.setText(">");
                }
                mediaPlayer = new MediaPlayer(newValue.getData());
                index = songList.getItems().indexOf(newValue);
            }
        });

        songList.setOnKeyPressed(e -> {
            if(e.getCode() == KeyCode.ENTER){
                if(isPlaying){
                    mediaPlayer.pause();
                    isPlaying = false;
                    playButton.setText(">");
                }else{
                    play();
                }
            }
        });


        prevButton = new Button("<<");
        prevButton.setMinWidth(50);
        prevButton.setMaxWidth(50);
        prevButton.setOnAction(e -> prev());
        grid.add(prevButton, 0, 3, 2,1);

        playButton = new Button(">");
        playButton.setMinWidth(100);
        playButton.setMaxWidth(100);
        playButton.setOnAction(e -> play());
        grid.add(playButton,2,3,2,1);

        nextButton = new Button(">>");
        nextButton.setMinWidth(50);
        nextButton.setMaxWidth(50);
        nextButton.setOnAction(e -> next());
        grid.add(nextButton, 4, 3, 2, 1);

        downloadButton = new Button("Download");
        downloadButton.setMinWidth(100);
        downloadButton.setMaxWidth(100);
        downloadButton.setOnAction(e -> download());
//        grid.add(downloadButton, 6, 3, 2, 1);

        shuffleButton = new Button("Shuffle");
        shuffleButton.setMinWidth(100);
        shuffleButton.setMaxWidth(100);
        shuffleButton.setOnAction(e -> {
            if (!shuffled) {
                shuffle();
            } else {
                shuffled = false;
            }
        });
//        grid.add(shuffleButton, 10, 3, 2, 1);

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
        grid.add(volumeLabel, 0, 4, 6, 1);

        volumeSlider = new Slider();
        volumeSlider.setValue(1.0);
        volumeSlider.setMax(1.0);
        volumeSlider.setMin(0.0);
        volumeSlider.setMinWidth(100);
        volumeSlider.setMaxWidth(100);
        grid.add(volumeSlider, 6, 4, 6, 1);

        volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                mediaPlayer.setVolume(volumeSlider.getValue());
            }
        });

        Media song = DataSource.getAllSongs().get(0).getData();
        songList.getSelectionModel().select(0);
        songList.getFocusModel().focus(0);
        mediaPlayer = new MediaPlayer(song);
        mediaPlayer.stop();

        Scene scene = new Scene(grid, 470,400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void prev(){
        mediaPlayer.stop();
        if(!shuffled) {
            if (index == 0) {
                songList.getSelectionModel().select(songList.getItems().size() - 1);
            } else {
                songList.getSelectionModel().selectPrevious();
            }
        } else {
            shuffleIndex -= 1;
            if (shuffleIndex <= 0) {
                shuffleIndex = shuffleOrder.length-1;
            }
            songList.getSelectionModel().select(shuffleOrder[shuffleIndex]);
        }
        play();
    }

    public void next(){
        mediaPlayer.stop();
        if (!shuffled) {
            if (index == songList.getItems().size() - 1) {
                songList.getSelectionModel().select(0);
            } else {
                songList.getSelectionModel().selectNext();
            }
        } else {
            shuffleIndex += 1;
            if (shuffleIndex <= shuffleOrder.length) {
                if (shuffleIndex == shuffleOrder.length-1) {
                    shuffleIndex = 0;
                }
                songList.getSelectionModel().select(shuffleOrder[shuffleIndex]);
            }
        }
        play();
    }

    public void download(){

    }

    public void shuffle(){
        shuffled = true;
        Random rng = new Random();
        Set<Integer> rand = new LinkedHashSet<>();
        while (rand.size() < songList.getItems().size()-1) {
            Integer r = rng.nextInt(songList.getItems().size()-1);
            rand.add(r);
        }
        shuffleOrder = new int[rand.size()];
        int ind = 0;
        for (Iterator i = rand.iterator(); i.hasNext();) {
            shuffleOrder[ind++] = (int) i.next();
        }
    }

    public void play() {
        mediaPlayer.setOnReady(new Runnable() {
            @Override
            public void run() {
                timeSlider.setMax(mediaPlayer.getTotalDuration().toSeconds());
                timeSlider.setMin(0.0);
                timeSlider.setValue(0.0);
            }
        });
        mediaPlayer.setOnPaused(new Runnable() {
            @Override
            public void run() {
                playButton.setText(">");
                isPlaying = false;
            }
        });
        mediaPlayer.setOnPlaying(new Runnable() {
            @Override
            public void run() {
                playButton.setText("||");
                if (isAtEnd) {
                    mediaPlayer.seek(time.multiply(timeSlider.getValue()));
                    isAtEnd = false;
                }
                isPlaying = true;
            }
        });
        mediaPlayer.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                next();
            }
        });

        mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
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
        time = mediaPlayer.getCurrentTime();
        timeSlider.setValue(time.toSeconds());
        timeDisplay.setText(getFormattedTime(mediaPlayer.getCurrentTime()) + " / " + getFormattedTime(mediaPlayer.getTotalDuration()));
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