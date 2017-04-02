import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;


public class Main extends Application {

    public void start(Stage primaryStage) throws Exception{
        primaryStage.setTitle("Music Player - Server");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_LEFT);


        Scene scene = new Scene(grid, 450,400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args){
        launch(args);
    }
}
