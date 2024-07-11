package ntua.multimedia.minesweeper;

import javafx.application.Application;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import ntua.multimedia.minesweeper.menubar.MinesweeperMenuBar;

import java.io.IOException;

public class MinesweeperApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // this VBox has a menubar on top and the game area on the bottom
        VBox rootApp = new VBox();
        rootApp.getChildren().add(new MinesweeperMenuBar(rootApp));

        /* add a little descriptive text at the start */
        TextArea textArea = new TextArea();
        textArea.setPrefSize(500,350);
        textArea.setWrapText(true);
        textArea.setEditable(false);
        textArea.setFont(Font.font("JetBrains Mono", 14));
        textArea.setText(
                """
                        Hello!
    
                        Some information about the game:
                         - Revealed tiles show the number of neighboring mines.
                         - Flagged tiles show either a flag or the letter 'F'.
                         - Tiles with normal mines will display either a transparent mine
                           or the letter 'X'.
                         - Tiles with hyper mines will display a black mine or the letter 'H'.
                         - There is at most 1 hyper mine.
                         - Mines revealed via flagging the hyper mine are marked as safe!!
    
                        Good Luck!""");

        rootApp.getChildren().add(textArea);

        Scene scene = new Scene(rootApp);
        stage.setScene(scene);
        stage.setTitle("MediaLab Minesweeper");

        // listener to appropriately size our screen
        ListChangeListener<Node> listChangeListener = c -> stage.sizeToScene();
        rootApp.getChildren().addListener(listChangeListener);

        stage.sizeToScene();
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}