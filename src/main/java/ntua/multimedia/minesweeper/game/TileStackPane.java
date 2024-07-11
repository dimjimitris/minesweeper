package ntua.multimedia.minesweeper.game;

import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import ntua.multimedia.minesweeper.utilities.AlertMessage;
import ntua.multimedia.minesweeper.utilities.PathLogger;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

// graphical representation of a tile
public class TileStackPane extends StackPane {
    // tiles are squares of size by size
    private static final int size = 40;

    // coordinates of this tile
    final int x;
    final int y;

    // we only need one function from the Game object. It
    // is triggered whenever we click on a Tile. This is a very
    // simple way to achieve communication among the controlling object of the game
    // and the game tiles
    private final Game game;

    // the images we use... They are the same for all Tiles
    private static Image flag = null;
    private static Image mine = null;
    private static Image hyperMine = null;
    private static boolean loadedImages = false;

    // graphics related stuff
    private final Rectangle rectangle;
    private final ImageView imageView;
    private final Text text;

    TileStackPane(int X, int Y, Game g) {
        // get related game information
        x = X;
        y = Y;
        game = g;

        // If this is the first tile being created, load images...
        if (!loadedImages) {
            loadedImages = true;

            flag = loadImage(PathLogger.images + "/flag.png");
            mine = loadImage(PathLogger.images + "/mine.png");
            hyperMine = loadImage(PathLogger.images + "/hyperMine.png");
        }

        // graphics set-up
        rectangle = new Rectangle(size, size);
        rectangle.setStrokeWidth(size / 10);
        rectangle.setArcWidth(size / 8);
        rectangle.setArcHeight(size / 8);

        imageView = new ImageView();
        text = new Text();

        // all rectangles start as hidden
        setHidden();
        setAlignment(Pos.CENTER);
        getChildren().addAll(rectangle, imageView, text);

        // set event handler
        setOnMouseClicked(event -> game.handler(x, y, event.getButton()));
        // we needed the game field only to call this function, we do not care about other members of the Game object
    }

    // safe way to load an Image. We use a try-with-resource statement to guarantee that the resources are closed
    // whether reading succeeds or fails
    private static Image loadImage(String filePath) {
        try (FileInputStream inputStream = new FileInputStream(filePath);
             BufferedInputStream bIS = new BufferedInputStream(inputStream)) {
            return new Image(bIS);
        } catch (FileNotFoundException e) {
            AlertMessage alertMessage = new AlertMessage(Alert.AlertType.ERROR, "Loading Game Images", null, "Could not load game images.");
            alertMessage.show();
            return null;
        } catch (IOException e) {
            AlertMessage alertMessage = new AlertMessage(Alert.AlertType.ERROR, "Loading Game Images", null, "Could not close image files.");
            alertMessage.show();
            return null;
        }
    }

    void setHidden() {
        rectangle.setFill(Color.LIGHTGRAY);
        rectangle.setStroke(Color.DARKGRAY);
        imageView.setImage(null);
        imageView.setFitHeight(size);
        imageView.setFitWidth(size);
        text.setText("");
    }

    void setFlagged() {
        rectangle.setFill(Color.LIGHTCYAN);
        rectangle.setStroke(Color.SKYBLUE);
        imageView.setImage(flag);
        imageView.setFitHeight(size);
        imageView.setFitWidth(size);
        text.setText("");

        // in case a flag image could not be loaded
        if (flag == null) text.setText("F");
    }

    void setRevealNeutral(int mines) {
        rectangle.setFill(Color.WHITE);
        rectangle.setStroke(Color.LIGHTGRAY);
        imageView.setImage(null);
        imageView.setFitHeight(size);
        imageView.setFitWidth(size);

        String content = "";
        if (mines > 0) { // if there are neighboring mines, show them
            content += mines;
        }
        text.setText(content);
    }

    void setRevealMine(boolean unsafe) { setRevealMineGeneral(true, unsafe); }

    void setRevealHyperMine(boolean unsafe) { setRevealMineGeneral(false, unsafe); }

    private void setRevealMineGeneral(boolean isNormalMine, boolean unsafe) {
        if (unsafe) { // if a mine is revealed as unsafe you loose
            rectangle.setFill(Color.INDIANRED);
            rectangle.setStroke(Color.DARKRED);
        } else { // if a mine is revealed as not unsafe you may continue playing
            // this options helps with the hyper mine functionality we want in the game
            rectangle.setFill(Color.GREENYELLOW);
            rectangle.setStroke(Color.LIGHTSEAGREEN);
        }

        String textContent;
        if (isNormalMine) {
            imageView.setImage(mine);
            textContent = (mine == null) ? "X" : "";
        }
        else {
            imageView.setImage(hyperMine);
            textContent = (hyperMine == null) ? "H" : "";
        }

        imageView.setFitWidth(size);
        imageView.setFitHeight(size);

        // setting textContent like this allows us to accommodate the case of failure to load game images
        text.setText(textContent);
    }
}