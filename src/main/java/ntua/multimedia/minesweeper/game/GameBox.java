package ntua.multimedia.minesweeper.game;

import javafx.event.Event;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import ntua.multimedia.minesweeper.utilities.AlertMessage;
import ntua.multimedia.minesweeper.utilities.PathLogger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.min;

public class GameBox extends VBox {
    // game to which this GameBox belongs/refers to
    private final Game game;

    // size (9 or 16 depending on difficulty)
    private final int size;

    // a GameBox has a ToolBar and a GridPane with the game Tiles.
    final ToolBar toolBar;
    final TileStackPane[][] tiles;

    public GameBox(Game g, int timeRemaining, int flagsUsed, int totalMines, int attempts) {
        game = g;
        size = game.size;

        // set-up game area
        GridPane grid = new GridPane();
        tiles = new TileStackPane[size][size];
        for (int y = 0; y < size; ++y) {
            for (int x = 0; x < size; ++x) {
                tiles[x][y] = new TileStackPane(x, y, game); // create tiles
                grid.add(tiles[x][y],x,y); // add them to the grid
            }
        }

        // set up toolBar
        toolBar = new ToolBar(timeRemaining, flagsUsed, totalMines, attempts);

        getChildren().addAll(toolBar, grid);
    }

    // the game needs access to tiles and neighbors of tiles, so we provide these methods
    TileStackPane getTile(int x, int y) { return tiles[x][y]; }
    ArrayList<TileStackPane> neighbors(int x, int y) {
        ArrayList<TileStackPane> neighbors = new ArrayList<>();

        /* pairs of possible locations of neighbors */
        int[] points = new int[] {
                -1,-1, // top left
                -1, 0, // on my left
                -1, 1, // bottom left
                0,-1, // above me
                0, 1, // bellow me
                1,-1, // top right
                1, 0, // on my right
                1, 1  // bottom right
        };

        for (int i = 0; i < points.length; ++i) {
            int dx = points[i];
            int dy = points[++i];

            int newX = x + dx;
            int newY = y + dy;

            if (validCoords(newX, newY)) {
                neighbors.add(tiles[newX][newY]);
            }
        }
        // neighbors array does not include the (x,y) tile
        return neighbors;
    }
    boolean validCoords(int x, int y) { return x >= 0 && x < size && y >= 0 && y < size; }

    void endGame(boolean gameWon) {
        // area is no longer clickable
        addEventFilter(MouseEvent.ANY, Event::consume);

        // pop up message
        String[] info = new String[2];
        if (gameWon) {
            info[0] = "Game Completed";
            info[1] = "You won!";
        }
        else {
            info[0] = "Game Over";
            info[1] = "You lost...";
        }

        AlertMessage alertMessage = new AlertMessage(Alert.AlertType.INFORMATION, info[0], null, info[1]);
        alertMessage.show();

        // create new entry
        createRoundsTxt(gameWon);
    }

    // creates the mines.txt file. Tile in top left corner is (0,0). The first number is the row, the second number
    // is the column and the third number 1 (hyper mine) or 0 (normal mine). We use a try-with-resources statement
    // to ensure that the close method is properly called at all cases.
    void createMinesTxt(ArrayList<Game.Tile> mineList) {
        // in the mineList the first Tiles will be hypermines because of the way we constructed it
        int i = game.hyperMines;
        StringBuilder outputString = new StringBuilder();
        if (mineList != null) {
            for (Game.Tile T : mineList) {
                int row = T.getY();
                int column = T.getX();
                int isHyper = 0;
                if (i > 0) isHyper = 1;

                outputString
                        .append(row).append(", ")
                        .append(column).append(", ")
                        .append(isHyper).append("\n");

                --i;
            }
        }
        else {
            outputString.append("This text file will contain the locations of mines after your first left click!");
        }

        try (FileWriter fileWriter = new FileWriter(PathLogger.medialab + "/mines.txt", false);
             BufferedWriter writer = new BufferedWriter(fileWriter)){

            writer.write(outputString.toString());
        } catch (IOException e) {
            AlertMessage alertMessage = new AlertMessage(Alert.AlertType.ERROR, "Mines file creation", null, "There was an error in accessing or writing to mines.txt");
            alertMessage.show();
        }
    }

    // after we complete playing through a round we register the results for this game
    // we keep track of the 5 most recent rounds, with the most recent being at the top of "rounds.txt" file.
    // We use try-with-resources statements to ensure that close is properly called at all cases.
    void createRoundsTxt(boolean gameWon) {
        String newEntry = "" + game.totalMines +
                " " + game.getAttempts() +
                " " + (game.time - game.getTimeRemaining()) +
                " " + (gameWon ? "player" : "computer");

        List<String> lines = new ArrayList<>();
        lines.add(newEntry);

        try (FileReader fileReader = new FileReader(PathLogger.rounds);
             BufferedReader reader = new BufferedReader(fileReader)){

            String line = reader.readLine();
            while(line != null) {
                lines.add(line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            AlertMessage alertMessage = new AlertMessage(Alert.AlertType.INFORMATION, "Rounds file Access", null, "Save game file did not exist.");
            alertMessage.show();
        }

        try (FileWriter fileWriter = new FileWriter(PathLogger.rounds, false);
             BufferedWriter writer = new BufferedWriter(fileWriter)) {

            for (int i = 0; i < min(5, lines.size()); ++i) {
                writer.write(lines.get(i));
                writer.newLine();
            }
            AlertMessage alertMessage = new AlertMessage(Alert.AlertType.INFORMATION, "Rounds file Entry", null, "Game results saved.");
            alertMessage.show();

        } catch (IOException e) {
            AlertMessage alertMessage = new AlertMessage(Alert.AlertType.ERROR, "Rounds file Access", null, "Could not save game.");
            alertMessage.show();
        }
    }
}
