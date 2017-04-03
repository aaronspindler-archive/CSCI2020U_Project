import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;


public class Main extends Application {
    Button play;
    Slider timeSlider;
    MediaPlayer mediaPlayer;
    Duration time;
    Boolean isPlaying = false;
    Boolean isAtEnd = false;

    public void start(Stage primaryStage) throws Exception{
        primaryStage.setTitle("Music Player - Client");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_LEFT);

        play = new Button(">");
        play.setOnAction(e -> play());
        timeSlider = new Slider();
        String file = "excellent.mp3";
        Media song = new Media(new File(file).toURI().toString());
        mediaPlayer = new MediaPlayer(song);
        grid.add(play,0,0,1,1);
        grid.add(timeSlider, 0,2,2,1);

        Scene scene = new Scene(grid, 450,400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void play() {
        timeSlider.setMax(mediaPlayer.getTotalDuration().toSeconds());
        timeSlider.setMin(0.0);
        timeSlider.setValue(mediaPlayer.getStartTime().toSeconds());
        time = mediaPlayer.getStartTime();
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
    }

    public static void main(String[] args){
        launch(args);
    }
}
