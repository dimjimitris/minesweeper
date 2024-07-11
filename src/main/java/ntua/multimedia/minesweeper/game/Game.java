package ntua.multimedia.minesweeper.game;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.input.MouseButton;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * The <code>Game</code> class implements the logic of the Minesweeper game and its graphical components.
 * <p></p>
 * All of the above are used by this class and classes in the <code>game</code> package to ensure
 * correct communication between different objects and achieve an appropriate user experience.
 * <p></p>
 * This class and its public methods are to be used by the rest of the application, which should be
 * outside the <code>game</code> package, in order to manage Minesweeper's behaviour, view and gameplay.
 * <p></p>
 * @author Dimitrios Georgousis (NTUA ECE, AM: 03119005)
 * @version 1.1
 */
public class Game {
    // size of game - grid, 9 or 16 depending on difficulty, but it doesn't matter
    final int size;
    final int totalMines;

    // total time we can play this game, before we lose
    final int time;

    // how many hypermines there are
    final int hyperMines;

    private int flagsUsed;

    // number of attempts to reveal tiles the user has made (left-clicks)
    private int attempts;

    // timer
    private final Timeline timeline;

    // Information about all the tiles in the game-grid
    private final Tile[][] tiles;
    private ArrayList<Tile> mineList;

    // counts how many unrevealed non-mine (neutral) tiles remain in the game
    private int neutralRemaining;
    private int timeRemaining;

    private final GameBox gameBox;

    // helps to ensure that a game may end only once
    private boolean gameEnded;

    // used to control actions on first click...
    // If it is true, it means the user has already managed to reveal some tiles!
    private boolean gameStarted;

    private Game(int Size, int TotalMines, int Time, int HyperMines) {
        // set up logic of this game
        size = Size;
        totalMines = TotalMines;
        time = Time;
        hyperMines = HyperMines;
        flagsUsed = 0;
        attempts = 0;
        timeline = new Timeline();

        neutralRemaining = (size * size) - totalMines;
        timeRemaining = time;

        // set up graphics
        gameBox = new GameBox(this, timeRemaining, flagsUsed, totalMines, attempts);

        // create our tile grid that carries the game logic
        tiles = new Tile[size][size];
        for (int y = 0; y < size; ++y) {
            for (int x = 0; x < size; ++x) {
                tiles[x][y] = new Tile(gameBox.getTile(x,y));
            }
        }

        // timeline updates every second showing the remaining game time...
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1), event -> {
            updateTimeLabel(-1);
            if (timeRemaining <= 0) {
                endGame(false);
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);

        // game has now started
        gameEnded = false;

        // game will start when the player does their first action that reveals any tiles...
        gameStarted = false;

        // create a random game...
        completeFill(randomPos(), randomPos());
    }

    // completes the filling of the game grid by setting the appropriate types of tiles
    private void completeFill(int x, int y) {
        if (gameStarted) return;
        // when we set the types we also know the locations of mines
        mineList = setTypes(x, y);
        // create the mines.txt file since mines are now known
        gameBox.createMinesTxt(mineList);
    }

    // Tile (x,y) and its neighbors are safe aka they are all neutral tiles
    // we then randomize the rest of tiles...
    private ArrayList<Tile> setTypes(int x, int y) {
        if (gameStarted) return mineList;
        // clicked and its neighbors are safe
        ArrayList<TileStackPane> safeTiles = new ArrayList<>();
        safeTiles.add(gameBox.getTile(x, y));
        safeTiles.addAll(gameBox.neighbors(x, y));

        // create a list of all tiles
        ArrayList<TileStackPane> list = new ArrayList<>();
        for (int j = 0; j < size; ++j) {
            for (int i = 0; i < size; ++i) {
                list.add(gameBox.getTile(i, j));
            }
        }

        // remove the safe tiles from this list
        list.removeAll(safeTiles);

        // randomly shuffle the rest of the tiles
        Collections.shuffle(list);

        // append at the end the safeTiles
        list.addAll(safeTiles);

        // array where all the mines are stored
        ArrayList<Tile> result = new ArrayList<>();
        for (int i = 0; i < hyperMines; ++i) {
            TileStackPane T = list.get(i);
            tiles[T.x][T.y].setType(TileType.HYPERMINE);
            result.add(tiles[T.x][T.y]);
        }
        for (int i = hyperMines; i < totalMines; ++i) {
            TileStackPane T = list.get(i);
            tiles[T.x][T.y].setType(TileType.MINE);
            result.add(tiles[T.x][T.y]);
        }
        for (int i = totalMines; i < size*size; ++i) {
            TileStackPane T = list.get(i);
            tiles[T.x][T.y].setType(TileType.NEUTRAL);
        }
        return result;
    }

    // simply calculates the neighboring mines and returns that number
    private int calcMines(int x, int y) {
        int mines = 0;
        for (TileStackPane T : gameBox.neighbors(x,y)) {
            if (tiles[T.x][T.y].type != TileType.NEUTRAL) ++mines;
        }
        return mines;
    }

    // this function is called by the event handlers of TileStackPanes whenever a click event happens...
    // It is a very simple way to achieve communication between the game classes.
    void handler(int x, int y, MouseButton button) {
        Tile tile = tiles[x][y];

        // actions on revealed tiles have no effect
        if (tile.state == TileState.REVEALED) return;

        switch (button) {
            case PRIMARY -> {
                // left-click on a FLAGGED tile has no effect
                if (tile.state == TileState.FLAGGED) return;

                // first left-click must be on a neutral tile with no
                // neighboring mines. If this is not true, just change the game...
                if (!gameStarted && (tile.type != TileType.NEUTRAL || calcMines(x,y) != 0)) {
                    completeFill(x,y);
                }

                // game has now started, we will reveal some tiles
                gameStarted = true;
                updateAttemptsLabel(1);
                recurse(x, y);

                // if we revealed a mine with a left-click then we lose
                if (tile.type != TileType.NEUTRAL) {
                    endGame(false);
                    return;
                }
            }
            case SECONDARY -> {
                tile.flagTile();

                // if we flagged a hypermine and the appropriate condition is true, we invoke its effect
                if (hyperMineActivation(tile)) {
                    // game has started since we will reveal some tiles
                    gameStarted = true;
                    revealHyperMine(x, y);
                }
            }
            default -> {}
        }

        // if we didn't lose and the click (left or right) was successful, check whether we won...
        if (neutralRemaining <= 0) {
            endGame(true);
        }
    }

    private boolean hyperMineActivation(Tile tile) {
        return tile.type == TileType.HYPERMINE && tile.state == TileState.FLAGGED && attempts <= 4;
    }

    // classic minesweeper recursion
    private void recurse(int x, int y) {
        Tile tile = tiles[x][y];
        // if recursion has already executed on this tile just return
        if (tile.recursed) return;
        // we are going to do recursion on this tile, so mark the corresponding property as true
        tile.recursed = true;
        tile.revealTile(true); // unsafe = true, because if we left-clicked on a mine we might lose
                                     // if the tile has already been revealed, this function will do nothing
        if (tile.type != TileType.NEUTRAL) return; // if it was a mine we lose

        if (calcMines(x, y) == 0) { // if all neighbors are neutral, do recursion on them
            for (TileStackPane T : gameBox.neighbors(x,y)) recurse(T.x, T.y);
        }
    }

    // hypermine effect
    private void revealHyperMine(int x, int y) {
        for (int i = 0; i < size; ++i) {
            tiles[x][i].revealTile(false); // tiles on the same column
            tiles[i][y].revealTile(false); // tiles on the same row
        }
    }

    // simple wrappers for updating {time,flags,attempts}Label
    private void updateTimeLabel(int inc) {
        timeRemaining += inc;
        gameBox.toolBar.setTimeLabel(timeRemaining);
    }
    private void updateFlagsLabel(int inc) {
        flagsUsed += inc;
        gameBox.toolBar.setFlagsLabel(flagsUsed, totalMines);
    }

    private void updateAttemptsLabel(int inc) {
        attempts += inc;
        gameBox.toolBar.setAttemptsLabel(attempts);
    }

    private void endGame(boolean gameWon) {
        // this method can be called only once
        if (gameEnded) return;
        gameEnded = true;

        // timer should stop
        timeline.stop();

        // show all mines
        revealMines(!gameWon);

        // graphics set
        gameBox.endGame(gameWon);
    }

    private void revealMines(boolean gameOver) { for (Tile T : mineList) T.revealTile(gameOver); }

    int getTimeRemaining() { return timeRemaining; }

    // class that holds logic about the tiles
    class Tile {
        private final TileStackPane tilePane; // TileStackPane to which this Tile is "connected"
        private TileState state;
        private TileType type;
        private boolean recursed;

        private Tile(TileStackPane tileStackPane) {
            tilePane = tileStackPane;
            state = TileState.HIDDEN;
            type = TileType.UNASSIGNED;
            recursed = false;
        }

        // flags a hidden tile or hides a flagged tile...
        private void flagTile() {
            TileState newState = state;
            if (state == TileState.HIDDEN && flagsUsed < totalMines) {
                updateFlagsLabel(1);
                tilePane.setFlagged();
                newState = TileState.FLAGGED;
            }
            else if (state == TileState.FLAGGED) {
                updateFlagsLabel(-1);
                tilePane.setHidden();
                newState = TileState.HIDDEN;
            }
            state = newState; // set the tile's state appropriately
            // if (state == TileState.REVEALED) do nothing!!!
        }

        // reveals the tile based on its type and state
        private void revealTile(boolean unsafe) {
            if (type == TileType.UNASSIGNED) return;
            // if already revealed do nothing
            if (state == TileState.REVEALED) return;

            switch (type) {
                case NEUTRAL -> {
                    if (state == TileState.FLAGGED) { // when revealing neutral tiles, remove their flags
                        updateFlagsLabel(-1);
                    }

                    --neutralRemaining; // since we reveal a neutral tile, there's one less remaining...
                    tilePane.setRevealNeutral(calcMines(tilePane.x, tilePane.y));
                }
                case MINE -> {
                    if (state == TileState.HIDDEN) { // when revealing a mine, we consume a flag
                        updateFlagsLabel(1);
                    }
                    tilePane.setRevealMine(unsafe);
                }
                case HYPERMINE -> {
                    if (state == TileState.HIDDEN) { // when revealing a mine, we consume a flag
                        updateFlagsLabel(1);
                    }
                    tilePane.setRevealHyperMine(unsafe);
                }
            }
            state = TileState.REVEALED; // tile is now REVEALED
        }

        private void setType(TileType tileType) {
            if (gameStarted) return; // once the game has started Tiles must be set!
            type = tileType;
        }

        int getX() { return tilePane.x; }
        int getY() { return tilePane.y; }
    }

    private enum TileState { HIDDEN, FLAGGED, REVEALED }
    private enum TileType { NEUTRAL, MINE, HYPERMINE, UNASSIGNED }

    private int randomPos() {
        Random ran = new Random();
        int num = ran.nextInt(size);
        if (num >= 0 && num < size) {
            return num;
        }
        else
            return size/2;
    }

    /**
     * Constructs a <code>Game</code> based on a specified description provided in {@param gameParams} array.
     * <p></p>
     * A game of minesweeper is characterized by its description which must be known at creation time. This constructor
     * takes as input an array of integers (<code>int</code>) representing a valid game description and creates a game
     * based on this description.
     * <p></p>
     * One should know that the location of mines on the minefield is not only randomized but is chosen based on the
     * location of the first valid left-click of the user ensuring that a user may never lose immediately because of
     * bad luck. This, however, may not affect the outside behaviour of a <code>Game</code> object created with this
     * constructor.
     * <p></p>
     * @param gameParams        An array of integers (<code>int</code>) which describes the game we wish to create.
     *                          <code>gameParams.length</code> must be at least four. If it is any bigger, numbers
     *                          following the first four do not matter.
     *                          <p>
     *                          The first 4 integers are <code>{ size, totalMines, time, hyperMines }</code>.
     *                          <ul>
     *                          <li><code>size</code>: The minesweeper grid or game area is a size by size square</li>
     *                          <li><code>totalMines</code>: The total number of tiles that are mines in the game</li>
     *                          <li><code>time</code>: Initial value of the countdown for each round. If a player takes longer
     *                              without beating the game then they automatically lose</li>
     *                          <li><code>hyperMines</code>: The number of hyper mines present in the constructed <code>Game</code>
     *                              object</li>
     *                          </ul>
     */
    public Game(int[] gameParams) { this(gameParams[0], gameParams[1], gameParams[2], gameParams[3]); }

    /**
     *
     * While playing a user left-clicks on tiles in their attempt to unveil all the neutral tiles and win; This function gets these attempts.
     * <p></p>
     * @return                  The number of attempts the user has made so far, while playing the game.
     */
    public int getAttempts() { return  attempts; }

    /**
     * <code>GameBox</code> is the graphical component (grid and some tools) of the minesweeper game.
     * <p></p>
     * All the logic of a minesweeper game acts upon specific graphical classes. This function may be used in efforts
     * to manipulate such classes.
     * <p></p>
     * For example: in order to format the visual experience of a player.
     * <p></p>
     * @return                  Returns a JavaFX Pane, specifically a {@link GameBox GameBox}, which contains the main
     *                          graphical/visual interface of the game.
     */
    public GameBox getGameBox() { return gameBox; }

    /**
     * This function starts that timer or lets it continue the countdown if stopped.
     * <p></p>
     * Each round (or game) of minesweeper has a reverse timer (countdown) in which a player must manage to beat the
     * game.
     * <p></p>
     * May be used in conjunction with {@link #stopTime() stopTime} to control the flow of the game.
     */
    public void startTime() { timeline.play(); }

    /**
     * A game has a countdown; this function may be used to halt that countdown.
     * <p></p>
     * For example: A player is allowed to start a different game than the one that they are currently playing
     * at any time. The current <code>Game</code> object may be discarded, but before that its timer should be stopped
     * in case another component or <code>package</code> or <code>class</code> wishes to act upon said object.
     * <p></p>
     * May be used in conjunction with {@link #startTime() startTime} to control the flow of the game.
     */
    public void stopTime() { timeline.stop(); }

    /**
     * This function permanently halts a game of minesweeper.
     * <p></p>
     * It has a visual side effect of showing all the mines in the minesweeper game and will,when called,
     * register this round as a loss for the player.
     * <p></p>
     * For example: A player gives up and wishes to view the solution to the round they were player.
     */
    public void stopGame() { endGame(false); }
}

// the implementation of method revealTile is quite expandable, if we wished for more actions we could just
// write a function for each case of the switch statement. Furthermore, if we wished for more cases we could
// implement this through an interface and class Tile would call the method of a class that implements that
// interface; this way we would also avoid a large switch statement and not have bloated code.
// This program is very simple though, so we don't need anything fancy like that...