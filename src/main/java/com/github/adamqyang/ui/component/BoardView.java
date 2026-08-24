package com.github.adamqyang.ui.component;

import com.github.adamqyang.chess.Position;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

/**
 * Renders a Position as an 8x8 board using Unicode chess glyphs - no image
 * assets, no external chess GUI library, no move-legality logic (this only
 * ever displays positions it's told to, it never validates them). Reused for
 * both the live FEN preview on the input screen and, later, solution replay
 * on the results screen.
 */
public final class BoardView extends GridPane {

    private static final double SQUARE_SIZE = 44;

    private final Label[][] squareLabels = new Label[Position.SIZE][Position.SIZE];

    public BoardView() {
        buildGrid();
    }

    private void buildGrid() {
        for (int rank = 0; rank < Position.SIZE; rank++) {
            for (int file = 0; file < Position.SIZE; file++) {
                StackPane square = new StackPane();
                square.setPrefSize(SQUARE_SIZE, SQUARE_SIZE);
                boolean isLightSquare = (rank + file) % 2 == 0;
                square.setStyle("-fx-background-color: " + (isLightSquare ? "#f0d9b5" : "#b58863") + ";");

                Label pieceLabel = new Label();
                pieceLabel.setStyle("-fx-font-size: 26px;");
                StackPane.setAlignment(pieceLabel, Pos.CENTER);
                square.getChildren().add(pieceLabel);

                squareLabels[rank][file] = pieceLabel;
                add(square, file, rank);
            }
        }
    }

    /** Displays the given position. */
    public void show(Position position) {
        for (int rank = 0; rank < Position.SIZE; rank++) {
            for (int file = 0; file < Position.SIZE; file++) {
                squareLabels[rank][file].setText(glyphFor(position.pieceAt(rank, file)));
            }
        }
    }

    /** Clears the board to all-empty squares. */
    public void clear() {
        for (Label[] row : squareLabels) {
            for (Label label : row) {
                label.setText("");
            }
        }
    }

    private static String glyphFor(char piece) {
        return switch (piece) {
            case 'K' -> "\u2654";
            case 'Q' -> "\u2655";
            case 'R' -> "\u2656";
            case 'B' -> "\u2657";
            case 'N', 'S' -> "\u2658";
            case 'P' -> "\u2659";
            case 'k' -> "\u265A";
            case 'q' -> "\u265B";
            case 'r' -> "\u265C";
            case 'b' -> "\u265D";
            case 'n', 's' -> "\u265E";
            case 'p' -> "\u265F";
            default -> ""; // empty square, or anything unrecognized
        };
    }
}
