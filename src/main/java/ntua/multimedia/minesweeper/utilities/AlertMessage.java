package ntua.multimedia.minesweeper.utilities;

import javafx.scene.control.Alert;

// class used for graphical alerts to users of our app.
// They are mostly informative (AlertType == INFORMATION) or about unwelcome behavior (AlertType == ERROR)
// They may be used about other types of alerts though too!!!
public class AlertMessage extends Alert {
    public AlertMessage(
            AlertType alertType, String title, String headerText, String contentText
    ) {
        super(alertType);
        setTitle(title);
        setHeaderText(headerText);
        setContentText(contentText);
    }
}
