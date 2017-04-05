import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;


public class Main extends Application {

    static final String baseDirectory = "clientMusic/";

    MenuBar menuBar;
    Menu fileMenu;
    MenuItem exitMenuItem;
    Button play, prev, next;
    Slider timeSlider, volumeSlider;
    Label timeDisplay, volumeLabel;
    ListView<Song> songList;
    int index = 0;

    MediaPlayer mediaPlayer;
    Duration time;
    Boolean isPlaying = false;
    Boolean isAtEnd = false;

    public void start(Stage primaryStage) throws Exception{
        primaryStage.setTitle("Music Player - Client");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_LEFT);

        menuBar = new MenuBar();
        fileMenu = new Menu("File");

        exitMenuItem = new MenuItem("Exit");
        exitMenuItem.setAccelerator(KeyCombination.keyCombination("Ctrl+Q"));
        exitMenuItem.setOnAction(e -> System.exit(0));

        fileMenu.getItems().add(exitMenuItem);
        menuBar.getMenus().add(fileMenu);

        grid.add(menuBar,0,0,8,1);

        songList = new ListView<Song>();
        songList.setMinWidth(300);
        songList.setMaxWidth(300);
        songList.setItems(DataSource.getAllSongs());
        grid.add(songList, 0, 1, 6,1);

        songList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Song>() {
            public void changed(ObservableValue<? extends Song> observable, Song oldValue, Song newValue) {
                if(isPlaying){
                    mediaPlayer.stop();
                    isPlaying = false;
                    isAtEnd = false;
                    play.setText(">");
                }
                mediaPlayer = new MediaPlayer(newValue.getData());
                index = songList.getItems().indexOf(newValue);
            }
        });

        volumeLabel = new Label("Volume: ");
        grid.add(volumeLabel, 7, 1, 1, 1);

        volumeSlider = new Slider();
        volumeSlider.setValue(1.0);
        volumeSlider.setMax(1.0);
        volumeSlider.setMin(0.0);
        grid.add(volumeSlider, 8, 1, 1, 1);

        volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                mediaPlayer.setVolume(volumeSlider.getValue());
            }
        });

        prev = new Button("<<");
        prev.setMinWidth(50);
        prev.setMaxWidth(50);
        prev.setOnAction(e -> prev());
        grid.add(prev, 0, 2, 1,1);

        play = new Button(">");
        play.setMinWidth(50);
        play.setMaxWidth(50);
        play.setOnAction(e -> play());
        grid.add(play,1,2,1,1);

        next = new Button(">>");
        next.setMinWidth(50);
        next.setMaxWidth(50);
        next.setOnAction(e -> next());
        grid.add(next, 2, 2, 1, 1);

        timeSlider = new Slider();
        timeSlider.setMinWidth(250);
        timeSlider.setMaxWidth(250);
        grid.add(timeSlider, 3,2,4,1);

        timeDisplay = new Label("0:0 / 0:0");
        grid.add(timeDisplay, 8,2,2,1);


        String file = "excellent.mp3";
        Media song = new Media(new File(baseDirectory + file).toURI().toString());
        mediaPlayer = new MediaPlayer(song);


        Scene scene = new Scene(grid, 500,400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void prev(){
        mediaPlayer.stop();
        incrementIndex(-1);
        String file = songList.getItems().get(index).getFileName();
        System.out.println(file);
        Media song = new Media(new File(baseDirectory + file).toURI().toString());
        mediaPlayer = new MediaPlayer(song);
        isPlaying = false;
        isAtEnd = false;
        play();
    }

    public void next(){
        mediaPlayer.stop();
        incrementIndex(1);
        String file = songList.getItems().get(index).getFileName();
        Media song = new Media(new File(baseDirectory + file).toURI().toString());
        mediaPlayer = new MediaPlayer(song);
        isPlaying = false;
        isAtEnd = false;
        play();
    }

    public void incrementIndex(int shift) {
        if (endOfList(shift)) {
            index = 0;
        } else if (startOfList(shift)) {
            index = songList.getItems().size()-1;
        } else {
            index += shift;
        }
    }

    public boolean endOfList(int shift) {
        return (index+shift) > songList.getItems().size()-1;
    }

    public boolean startOfList(int shift) {
        return (index+shift) < 0;
    }

    public void play() {
        timeSlider.setMax(mediaPlayer.getTotalDuration().toSeconds());
        timeSlider.setMin(0.0);
        timeSlider.setValue(mediaPlayer.getStartTime().toSeconds());
        mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                updateValues();
            }
        });

        if (!isPlaying) {
            play.setText("||");
            if (isAtEnd) {
                mediaPlayer.seek(time.multiply(timeSlider.getValue()));
                isAtEnd = false;
            }
            mediaPlayer.play();
            isPlaying = true;
        } else {
            mediaPlayer.pause();
            play.setText(">");
            isPlaying = false;
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

        return (minutes + ":" + seconds);
    }

    public static void main(String[] args){
        launch(args);
    }
}
