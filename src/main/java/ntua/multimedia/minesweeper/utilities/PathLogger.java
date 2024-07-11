package ntua.multimedia.minesweeper.utilities;

// a class that logs the paths to useful folders and provides them
// to the rest of the classes. We don't care about any instantiation of this class
public final class PathLogger {
    // path to resources
    private static final String resources = "src/main/resources";

    // path to medialab folder, contains game description .txt files and the mines.txt
    private static final String medialabLocal = "medialab";
    public static final String medialab = resources + "/" + medialabLocal;

    // path to rounds file, this file saves previous games played
    private static final String roundsLocal = "rounds/rounds.txt";
    public static final String rounds = resources + "/" + roundsLocal;

    //path to images folder, contains images used in the game
    private static final String imagesLocal = "images";
    public static final String images = resources + "/" + imagesLocal;
}
