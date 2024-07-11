package ntua.multimedia.minesweeper.menubar;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

// a clas which checks our errors and formats the game parameters of valid games
public class ErrorChecker {
    private static final DescriptionRules[] dRules = new DescriptionRules[] {
      new DescriptionRules(9,11,120,180,0, 9),
      new DescriptionRules(35,45,240,360,1,16)
    };

    private final int[] params;

    ErrorChecker(String path) throws InvalidDescriptionException, InvalidValueException, IOException {
        String[] lines = descriptionChecker(path);
        params = valueChecker(lines);
    }

    private static String[] descriptionChecker(String path) throws InvalidDescriptionException, IOException {
        try (FileReader fileReader = new FileReader(path);
             BufferedReader reader = new BufferedReader(fileReader)) {

            String[] lines = new String[4];
            String line;
            int i = 0;
            while ((line = reader.readLine()) != null) {
                if (i < 4) lines[i] = line;
                ++i;
                if (i >= 5) break;
            }
            if (i != 4) throw new InvalidDescriptionException(i);
            return lines;
        }
    }

    private static int[] valueChecker(String[] gameDescription) throws InvalidValueException {
        boolean[] errors = new boolean[] {false, false, false, false};
        int size, mines, time, hyperMine;

        int index = checkIndex(gameDescription[0]);
        errors[0] = index < 0;
        if (errors[0]) {
            throw new InvalidValueException(errors);
        }

        size = dRules[index].size();

        mines = checkMines(index, gameDescription[1]);
        errors[1] = mines < 0;

        time = checkTime(index, gameDescription[2]);
        errors[2] = time < 0;

        hyperMine = checkHyper(index, gameDescription[3]);
        errors[3] = hyperMine < 0;

        if (errors[1] || errors[2] || errors[3]) {
            throw new InvalidValueException(errors);
        }

        return new int[] {size, mines, time, hyperMine};
    }

    private static int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) { // it wasn't even a number
            return -1;
        }
    }

    private static int checkIndex(String difficulty) {
        int index = parseInt(difficulty) - 1;
        if (index >= 0 && index < dRules.length) {
            return index;
        }
        else return -1;
    }

    private static int checkMines(int index, String mines) {
        int mineCount = parseInt(mines);
        if (mineCount >= dRules[index].minesLower() && mineCount <= dRules[index].minesUpper()) {
            return mineCount;
        }
        else return -1;
    }

    private static int checkTime(int index, String time) {
        int timeCount = parseInt(time);
        if (timeCount >= dRules[index].timeLower() && timeCount <= dRules[index].timeUpper()) {
            return timeCount;
        }
        else return -1;
    }

    private static int checkHyper(int index, String hyper) {
        int hyperCount = parseInt(hyper);
        if (hyperCount >= 0 && hyperCount <= dRules[index].hyperMines()) {
            return hyperCount;
        }
        else return -1;
    }

    int[] getParams() { return params.clone(); }

    // records are basically used as simple classes with setters and getters here...
    private record DescriptionRules(int minesLower, int minesUpper, int timeLower, int timeUpper, int hyperMines, int size) {}
}
