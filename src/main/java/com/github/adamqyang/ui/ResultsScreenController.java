package com.github.adamqyang.ui;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Path;

import com.github.adamqyang.chess.Game;
import com.github.adamqyang.chess.Position;
import com.github.adamqyang.output.SolveResult;
import com.github.adamqyang.ui.component.BoardView;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class ResultsScreenController {

    @FXML private Label summaryLabel;
    @FXML private Button openOutputFileButton;
    @FXML private ListView<SolveResult.Solution> solutionListView;
    @FXML private BoardView resultBoard;
    @FXML private Button firstMoveButton;
    @FXML private Button prevMoveButton;
    @FXML private Label moveCounterLabel;
    @FXML private Button nextMoveButton;
    @FXML private Button lastMoveButton;
    @FXML private Label errorLabel;

    private Path outputFile;
    private Game currentGame;
    private int currentMoveIndex;

    @FXML
    public void initialize() {
        solutionListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(SolveResult.Solution item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    int index = getListView().getItems().indexOf(item) + 1;
                    setText("Solution " + index + " (" + item.moves().size() + " moves)");
                }
            }
        });
        solutionListView.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> {
                    if (newValue != null) {
                        selectSolution(newValue);
                    }
                });
    }

    /** Displays a completed solve's summary and populates the solution list. */
    public void showResult(SolveResult result, Path outputFile) {
        this.outputFile = outputFile;
        errorLabel.setText("");

        String verdictText = result.verdict() == SolveResult.Verdict.CORRECT ? "correct" : "cooked";
        StringBuilder text = new StringBuilder();
        text.append("Found ").append(result.solutionCount())
                .append(" solution").append(result.solutionCount() == 1 ? "" : "s");
        if (result.solvingTime() != null) {
            text.append(" in ").append(result.solvingTime());
        }
        text.append(" \u2014 the problem is ").append(verdictText).append(".");
        summaryLabel.setText(text.toString());

        openOutputFileButton.setDisable(false);

        solutionListView.getItems().setAll(result.solutions());
        if (!result.solutions().isEmpty()) {
            solutionListView.getSelectionModel().selectFirst();
        } else {
            currentGame = null;
            resultBoard.clear();
            moveCounterLabel.setText("No solution selected");
            setSteppingButtonsDisabled(true);
        }
    }

    private void selectSolution(SolveResult.Solution solution) {
        currentGame = new Game(Position.fromFen(Game.STANDARD_STARTING_FEN), solution.moves());
        currentMoveIndex = 0;
        updateBoardDisplay();
    }

    @FXML
    private void onFirstMoveClicked() {
        currentMoveIndex = 0;
        updateBoardDisplay();
    }

    @FXML
    private void onPrevMoveClicked() {
        if (currentGame != null && currentMoveIndex > 0) {
            currentMoveIndex--;
            updateBoardDisplay();
        }
    }

    @FXML
    private void onNextMoveClicked() {
        if (currentGame != null && currentMoveIndex < currentGame.moveCount()) {
            currentMoveIndex++;
            updateBoardDisplay();
        }
    }

    @FXML
    private void onLastMoveClicked() {
        if (currentGame != null) {
            currentMoveIndex = currentGame.moveCount();
            updateBoardDisplay();
        }
    }

    @FXML
    private void onOpenOutputFileClicked() {
        if (outputFile == null) {
            return;
        }
        try {
            Desktop.getDesktop().open(outputFile.toFile());
        } catch (IOException | UnsupportedOperationException e) {
            errorLabel.setText("Could not open the output file: " + e.getMessage());
        }
    }

    private void updateBoardDisplay() {
        if (currentGame == null) {
            return;
        }
        resultBoard.show(currentGame.positionAfter(currentMoveIndex));
        moveCounterLabel.setText("Move " + currentMoveIndex + " / " + currentGame.moveCount());

        boolean atStart = currentMoveIndex == 0;
        boolean atEnd = currentMoveIndex == currentGame.moveCount();
        firstMoveButton.setDisable(atStart);
        prevMoveButton.setDisable(atStart);
        nextMoveButton.setDisable(atEnd);
        lastMoveButton.setDisable(atEnd);
    }

    private void setSteppingButtonsDisabled(boolean disabled) {
        firstMoveButton.setDisable(disabled);
        prevMoveButton.setDisable(disabled);
        nextMoveButton.setDisable(disabled);
        lastMoveButton.setDisable(disabled);
    }
}