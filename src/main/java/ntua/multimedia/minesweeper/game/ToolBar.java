package ntua.multimedia.minesweeper.game;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class ToolBar extends GridPane {
    // some reasonable value for our label's height
    private static final int labelSize = 40;

    // 3 labels for the 3 measurements the user is required to be able to see
    private final Label timeLabel;      // time elapsed in the game
    private final Label flagsLabel;     // flags used
    private final Label attemptsLabel;  // left clicks performed

    public ToolBar(int time, int flags, int mines, int attempts) {
        // initialize tools
        timeLabel = new Label();
        setTimeLabel(time);
        timeLabel.setMinHeight(labelSize);
        timeLabel.setAlignment(Pos.CENTER);

        flagsLabel = new Label();
        setFlagsLabel(flags, mines);
        flagsLabel.setMinHeight(labelSize);
        flagsLabel.setAlignment(Pos.CENTER);

        attemptsLabel = new Label();
        setAttemptsLabel(attempts);
        attemptsLabel.setMinHeight(labelSize);
        attemptsLabel.setAlignment(Pos.CENTER);

        /* set up toolbar */
        setPadding(new Insets(0,10,0,10));
        setHgap(10);
        add(timeLabel,0,0);
        add(flagsLabel,1,0);
        add(attemptsLabel,2,0);
        setAlignment(Pos.CENTER);
    }

    // these functions are going to be used by the package as well
    void setTimeLabel(int timeRemaining) {
        timeLabel.setText("Time: " + timeRemaining + " seconds");
    }
    void setFlagsLabel(int flagsUsed, int mines) {
        flagsLabel.setText("Flags Used: " + flagsUsed + "/" + mines);
    }
    void setAttemptsLabel(int attempts) {
        attemptsLabel.setText("Attempts: " + attempts);
    }
}
