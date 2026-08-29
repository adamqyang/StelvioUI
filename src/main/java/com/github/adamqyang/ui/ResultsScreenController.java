package com.github.adamqyang.ui;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.adamqyang.chess.Game;
import com.github.adamqyang.chess.Position;
import com.github.adamqyang.config.MoveCount;
import com.github.adamqyang.output.SolveResult;
import com.github.adamqyang.ui.component.BoardView;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.FlowPane;

public class ResultsScreenController {

    private static final String MOVE_STYLE_NORMAL = "-fx-cursor: hand;";
    private static final String MOVE_STYLE_CURRENT =
            "-fx-cursor: hand; -fx-background-color: #cce5ff; -fx-font-weight: bold; -fx-padding: 0 2 0 2;";

    @FXML private Label summaryLabel;
    @FXML private Button openOutputFileButton;
    @FXML private ListView<SolveResult.Solution> solutionListView;
    @FXML private BoardView resultBoard;
    @FXML private Button firstMoveButton;
    @FXML private Button prevMoveButton;
    @FXML private Label moveCounterLabel;
    @FXML private Button nextMoveButton;
    @FXML private Button lastMoveButton;
    @FXML private FlowPane moveListPane;
    @FXML private TextField originalFenField;
    @FXML private TextField currentFenField;
    @FXML private TextArea pgnTextArea;
    @FXML private Label errorLabel;

    private Path outputFile;
    private Game currentGame;
    private int currentMoveIndex;

    // Keyed by moveIndex (1-based, matching Game.positionAfter's convention) -
    // lets highlightCurrentMove() find the right label directly rather than
    // scanning moveListPane's children every time.
    private final Map<Integer, Label> moveLabelsByIndex = new HashMap<>();
    private Label highlightedMoveLabel;

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
                    String length = MoveCount.ofHalfMoves(item.moves().size()).display();
                    setText("Solution " + index + " (" + length + " moves)");
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

    /** Displays a completed solve's summary, target FEN, and populates the solution list. */
    public void showResult(SolveResult result, SolveContext context) {
        this.outputFile = context.outputFile();
        originalFenField.setText(context.originalFen());
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
            moveListPane.getChildren().clear();
            moveLabelsByIndex.clear();
            moveCounterLabel.setText("No solution selected");
            currentFenField.setText("");
            pgnTextArea.setText("");
            setSteppingButtonsDisabled(true);
        }
    }

    private void selectSolution(SolveResult.Solution solution) {
        currentGame = new Game(Position.fromFen(Game.STANDARD_STARTING_FEN), solution.moves());
        currentMoveIndex = 0;
        pgnTextArea.setText(solution.rawText());
        buildMoveList(solution);
        updateBoardDisplay();
    }

    /**
     * Builds the clickable move list from the solution's raw text - reusing
     * the exact same tokenization MoveNotationParser applies internally
     * (split on whitespace, strip a leading "N." move-number prefix), so
     * this display-text list and solution.moves() stay in lockstep by
     * construction rather than needing to be kept manually in sync.
     */
    private void buildMoveList(SolveResult.Solution solution) {
        moveListPane.getChildren().clear();
        moveLabelsByIndex.clear();
        highlightedMoveLabel = null;

        List<String> moveTexts = new ArrayList<>();
        for (String token : solution.rawText().trim().split("\\s+")) {
            moveTexts.add(token.replaceFirst("^\\d+\\.", ""));
        }

        int moveNumber = 1;
        for (int i = 0; i < moveTexts.size(); i += 2) {
            Label numberLabel = new Label(moveNumber + ".");
            numberLabel.setStyle("-fx-text-fill: #888888;");
            moveListPane.getChildren().add(numberLabel);

            addMoveLabel(moveTexts.get(i), i + 1);
            if (i + 1 < moveTexts.size()) {
                addMoveLabel(moveTexts.get(i + 1), i + 2);
            }
            moveNumber++;
        }
    }

    private void addMoveLabel(String text, int moveIndex) {
        Label label = new Label(text);
        label.setStyle(MOVE_STYLE_NORMAL);
        label.setOnMouseClicked(e -> {
            currentMoveIndex = moveIndex;
            updateBoardDisplay();
        });
        moveListPane.getChildren().add(label);
        moveLabelsByIndex.put(moveIndex, label);
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

    @FXML
    private void onCopyOriginalFenClicked() {
        copyToClipboard(originalFenField.getText());
    }

    @FXML
    private void onCopyCurrentFenClicked() {
        copyToClipboard(currentFenField.getText());
    }

    @FXML
    private void onCopyPgnClicked() {
        copyToClipboard(pgnTextArea.getText());
    }

    private void copyToClipboard(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        Clipboard.getSystemClipboard().setContent(content);
    }

    /** Single source of truth for "currentMoveIndex changed" - called from every navigation path (buttons and clicks). */
    private void updateBoardDisplay() {
        if (currentGame == null) {
            return;
        }
        Position position = currentGame.positionAfter(currentMoveIndex);
        resultBoard.show(position);
        currentFenField.setText(position.toFen());
        moveCounterLabel.setText("Move " + currentMoveIndex + " / " + currentGame.moveCount());

        boolean atStart = currentMoveIndex == 0;
        boolean atEnd = currentMoveIndex == currentGame.moveCount();
        firstMoveButton.setDisable(atStart);
        prevMoveButton.setDisable(atStart);
        nextMoveButton.setDisable(atEnd);
        lastMoveButton.setDisable(atEnd);

        highlightCurrentMove();
    }

    private void highlightCurrentMove() {
        if (highlightedMoveLabel != null) {
            highlightedMoveLabel.setStyle(MOVE_STYLE_NORMAL);
        }
        Label current = moveLabelsByIndex.get(currentMoveIndex);
        if (current != null) {
            current.setStyle(MOVE_STYLE_CURRENT);
        }
        highlightedMoveLabel = current; // null at the starting position - nothing highlighted, which is correct
    }

    private void setSteppingButtonsDisabled(boolean disabled) {
        firstMoveButton.setDisable(disabled);
        prevMoveButton.setDisable(disabled);
        nextMoveButton.setDisable(disabled);
        lastMoveButton.setDisable(disabled);
    }
}