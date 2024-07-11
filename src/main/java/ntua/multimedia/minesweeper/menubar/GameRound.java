package ntua.multimedia.minesweeper.menubar;

// this class needed to be this way so that in order for the
// tableview to work properly
public class GameRound {
    private String mines;
    private String attempts;
    private String time;
    private String winner;

    public GameRound(String mines, String attempts, String time, String winner) {
        this.mines = mines;
        this.attempts = attempts;
        this.time = time;
        this.winner = winner;
    }

    /* setters and getters are needed for table representation I think */
    public String getMines() {
        return mines;
    }
    public String getAttempts() {
        return attempts;
    }
    public String getTime() {
        return time;
    }
    public String getWinner() {
        return winner;
    }

    public void setMines(String mines) {
        this.mines = mines;
    }
    public void setAttempts(String attempts) {
        this.attempts = attempts;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public void setWinner(String winner) {
        this.winner = winner;
    }
}
