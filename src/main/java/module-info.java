module ntua.multimedia.minesweeper {
    requires javafx.controls;

    opens ntua.multimedia.minesweeper.menubar to javafx.base;
    exports ntua.multimedia.minesweeper;
}