package ntua.multimedia.minesweeper.menubar;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ntua.multimedia.minesweeper.game.Game;
import ntua.multimedia.minesweeper.utilities.AlertMessage;
import ntua.multimedia.minesweeper.utilities.PathLogger;

import java.io.*;

public class MinesweeperMenuBar extends MenuBar {
    private final VBox rootApp; // VBox where the MenuBar and the game are hosted
    private Game game = null; // the game the user is currently playing
    private int[] gameParams = null; // 0 --> size, 1 --> mines, 2 --> time, 3 --> hyper mines

    public MinesweeperMenuBar(VBox RootApp) {
        rootApp = RootApp;

        // creating application menu
        Menu appMenu = new Menu("Application");

        MenuItem createItem = new MenuItem("Create");
        createItem.setOnAction(actionEvent -> createAct());

        MenuItem loadItem = new MenuItem("Load");
        loadItem.setOnAction(actionEvent -> loadAct());

        MenuItem startItem = new MenuItem("Start");
        startItem.setOnAction(actionEvent -> startAct());

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(actionEvent -> exitAct());

        appMenu.getItems().addAll(createItem, loadItem, startItem, exitItem);

        // creating details menu
        Menu detailsMenu = new Menu("Details");

        MenuItem roundsItem = new MenuItem("Rounds");
        roundsItem.setOnAction(actionEvent -> roundsAct());

        MenuItem solutionItem = new MenuItem("Solution");
        solutionItem.setOnAction(actionEvent -> solutionAct());

        detailsMenu.getItems().addAll(roundsItem, solutionItem);

        getMenus().addAll(appMenu, detailsMenu);
    }

    // As requested in the problem description the user gives input in the order:
    // SCENARIO-ID, Difficulty, Number of mines, Number of hypermines, Game Duration
    // We store data in the order: Difficulty, Number of mines, Game Duration, Number of hypermines
    private void createAct() {
        Stage stage = new Stage();
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle("Create Game File");

        Label fileIDLabel = new Label("SCENARIO-ID:");
        TextField fileIDField = new TextField();

        Label difficultyLabel = new Label("Difficulty:");
        TextField difficultyField = new TextField();

        Label minesLabel = new Label("Number of mines:");
        TextField minesField = new TextField();

        Label hyperMineLabel = new Label("Number of hypermines:");
        TextField hyperMineField = new TextField();

        Label timeLabel = new Label("Game Duration:");
        TextField timeField = new TextField();

        Label message = new Label("Fields should be integers.\n" +
                "If the SCENARIO-ID already exists, it is overwritten");

        Button createButton = new Button("Create");
        createButton.setOnAction(actionEvent -> {
            String fileName = fileIDField.getText().strip();
            String fileContent = difficultyField.getText().strip() + "\n" +
                                 minesField.getText().strip() + "\n" +
                                 timeField.getText().strip() + "\n" +
                                 hyperMineField.getText().strip(); // dif, mines, time, hypermines is the correct order of saving things
            fileContent = fileContent.strip();

            String filePath = PathLogger.medialab + "/" + fileName + ".txt";

            try (FileWriter fileWriter = new FileWriter(filePath, false)) {
                fileWriter.write(fileContent);

                stage.close();
            } catch (IOException e) {
                AlertMessage alertMessage = new AlertMessage(Alert.AlertType.ERROR, "Save Game Description", null, "Could not save this game.");
                alertMessage.show();
            }
        });

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10,10,10,10));
        grid.setVgap(8);
        grid.setHgap(10);
        grid.add(fileIDLabel,0,0);
        grid.add(fileIDField,1,0);
        grid.add(difficultyLabel,0,1);
        grid.add(difficultyField,1,1);
        grid.add(minesLabel,0,2);
        grid.add(minesField,1,2);
        grid.add(hyperMineLabel,0,3);
        grid.add(hyperMineField,1,3);
        grid.add(timeLabel,0,4);
        grid.add(timeField,1,4);
        grid.add(createButton,1,5);

        VBox box = new VBox(grid,message);
        box.setSpacing(8);

        Scene scene = new Scene(box, 300, 300);
        stage.setScene(scene);
        stage.show();
    }

    // checks if a game description is valid and initializes the gameParams array
    private void loadAct() {
        Stage stage = new Stage();
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle("Load Game Version");

        Label fileIDLabel = new Label("SCENARIO-ID:");
        TextField fileIDField = new TextField();

        Button loadButton = new Button("Load");
        loadButton.setOnAction(actionEvent -> {
            String filePath = PathLogger.medialab + "/" + fileIDField.getText().strip() + ".txt";

            try {
                ErrorChecker errorChecker = new ErrorChecker(filePath);
                gameParams = errorChecker.getParams();

                stage.close();
            }  catch (InvalidDescriptionException e) {
                AlertMessage alertMessage = new AlertMessage(Alert.AlertType.ERROR, "Game Description Error", null, e.getMyMessage());
                alertMessage.show();
            } catch (InvalidValueException e) {
                AlertMessage alertMessage = new AlertMessage(Alert.AlertType.ERROR, "Description Values Error", null, e.getMyMessage());
                alertMessage.show();
            } catch (IOException e) {
                AlertMessage alertMessage = new AlertMessage(Alert.AlertType.ERROR, "File Error", null, "Could not read game description file.");
                alertMessage.show();
            }
        });

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10,10,10,10));
        grid.setVgap(8);
        grid.setHgap(10);
        grid.add(fileIDLabel,0,0);
        grid.add(fileIDField,1,0);
        grid.add(loadButton,0,1);

        Scene scene = new Scene(grid, 300, 300);
        stage.setScene(scene);
        stage.show();
    }
    private void startAct() {
        if (gameParams == null) return; // no game description loaded

        if (game != null) { // game != null means that the user is already playing a game
            game.stopTime(); // stop the timeline! This game should no longer be running
            rootApp.getChildren().remove(game.getGameBox()); // remove the game
        }
        else if ((long) rootApp.getChildren().size() == 2) {
            rootApp.getChildren().remove(1); // remove the descriptive text in the start screen
        }

        game = new Game(gameParams); // create a new game
        rootApp.getChildren().add(game.getGameBox()); // new game should be visible to the user
        game.startTime(); // start the game after all these initializations are over
    }

    private void exitAct() {
        if (game != null) {
            game.stopTime();
        }
        Platform.exit();
    }

    // we are using a TableView to display previously completed rounds
    private void roundsAct() {
        TableView<GameRound> table = new TableView<>();

        TableColumn<GameRound, String> minesColumn = new TableColumn<>("Mines");
        minesColumn.setCellValueFactory(new PropertyValueFactory<>("mines"));

        TableColumn<GameRound, String> attemptsColumn = new TableColumn<>("Attempts");
        attemptsColumn.setCellValueFactory(new PropertyValueFactory<>("attempts"));

        TableColumn<GameRound, String> timeColumn = new TableColumn<>("Time");
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));

        TableColumn<GameRound, String> winnerColumn = new TableColumn<>("Winner");
        winnerColumn.setCellValueFactory(new PropertyValueFactory<>("winner"));

        table.getColumns().add(minesColumn);
        table.getColumns().add(attemptsColumn);
        table.getColumns().add(timeColumn);
        table.getColumns().add(winnerColumn);

        ObservableList<GameRound> data = FXCollections.observableArrayList();

        try (FileReader fileReader = new FileReader(PathLogger.rounds);
             BufferedReader reader = new BufferedReader(fileReader)) {

            String line;
            while ((line = reader.readLine()) != null) {
                // rounds are saved as mines attempts time winner, separated only by blank spaces
                String[] tokens = line.split(" ");
                data.add(new GameRound(
                        tokens[0], // mines
                        tokens[1], // attempts
                        tokens[2], // time
                        tokens[3])); // winner
            }
        } catch (IOException e) {
            AlertMessage alertMessage = new AlertMessage(Alert.AlertType.ERROR, "Error Retrieving Past Rounds", null, null);
            alertMessage.show();
        }

        table.setItems(data);

        Stage stage = new Stage();
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle("Rounds");

        Scene scene = new Scene(table);
        stage.setScene(scene);
        stage.show();
    }
    private void solutionAct() {
        if (game == null) return;
        game.stopGame();
    }
}
