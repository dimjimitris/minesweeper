package ntua.multimedia.minesweeper.menubar;

import java.util.Arrays;

// general error class
public class InvalidException extends Exception {
    protected String message = "";
    protected final static String[] gameSettings = new String[] {
            "Difficulty",
            "Mines",
            "Time",
            "Hyper mine"
    };

    protected InvalidException() { super(); }

    // our errors use the message string instead of error.getMessage() conventional method
    // because it is easier to manipulate
    public String getMyMessage() { return message; }
}

class InvalidValueException extends InvalidException {
    private final static String errorMsg = " field is not in the valid range";

    // errors is an array of 4 boolean values, each of which symbolizes that something went wrong
    // with the corresponding value. We show an error message only for the parameters that had a wrong value in the game description
    public InvalidValueException(boolean[] errors) {
        for (int i = 0; i < errors.length; ++i) {
            if (errors[i]) message += gameSettings[i] + errorMsg + "\n";
        }
    }
}

class InvalidDescriptionException extends InvalidException {
    private final static String errorMsg = " field could not be recognized";

    // lines is an index to the first line in the description txt file that had something wrong with it.
    // we display as mistaken all lines starting with this one.
    // We also consider as mistaken descriptions with more than 4 lines.
    public InvalidDescriptionException(int lines) {
        for (int i = lines; i < gameSettings.length; ++i) {
            message += gameSettings[i] + errorMsg + "\n";
        }

        if (lines >= gameSettings.length) {
            message += """
                    Too many lines, there must be 1 line for each of:
                     - Difficulty
                     - Number of mines
                     - Game time
                     - Presence of hypermine
                    """;
        }
    }
}
