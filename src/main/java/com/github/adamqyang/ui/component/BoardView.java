package com.github.adamqyang.ui.component;

import com.github.adamqyang.chess.Position;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

/**
 * Renders a Position as an 8x8 board. Pieces are drawn from SVG files (see
 * SvgPieceSet) when available, so their appearance doesn't depend on the
 * system's installed fonts. If a piece's SVG file hasn't been added yet,
 * this falls back to a Unicode glyph so the board still shows something
 * reasonable in the meantime - no code changes needed once real files land,
 * rendering upgrades automatically.
 */
public final class BoardView extends GridPane {

    private static final double SQUARE_SIZE = 44;
    private static final double PIECE_SIZE = 44;

    private final StackPane[][] squares = new StackPane[Position.SIZE][Position.SIZE];

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
                squares[rank][file] = square;
                add(square, file, rank);
            }
        }
    }

    /** Displays the given position. */
    public void show(Position position) {
        for (int rank = 0; rank < Position.SIZE; rank++) {
            for (int file = 0; file < Position.SIZE; file++) {
                renderPiece(squares[rank][file], position.pieceAt(rank, file));
            }
        }
    }

    /** Clears the board to all-empty squares. */
    public void clear() {
        for (StackPane[] row : squares) {
            for (StackPane square : row) {
                square.getChildren().clear();
            }
        }
    }

    private void renderPiece(StackPane square, char piece) {
        square.getChildren().clear();
        String resourceKey = resourceKeyFor(piece);
        if (resourceKey == null) {
            return; // empty square
        }

        Node svgNode = SvgPieceSet.createNode(resourceKey, PIECE_SIZE);
        if (svgNode != null) {
            square.getChildren().add(svgNode);
        } else {
            Label fallback = new Label(unicodeGlyphFor(piece));
            fallback.setStyle("-fx-font-size: 26px;");
            square.getChildren().add(fallback);
        }
    }

    /** Maps a FEN piece character to the wK/bK/etc. resource key SvgPieceSet expects. */
    private static String resourceKeyFor(char piece) {
        return switch (piece) {
            case 'K' -> "wK";
            case 'Q' -> "wQ";
            case 'R' -> "wR";
            case 'B' -> "wB";
            case 'N', 'S' -> "wN";
            case 'P' -> "wP";
            case 'k' -> "bK";
            case 'q' -> "bQ";
            case 'r' -> "bR";
            case 'b' -> "bB";
            case 'n', 's' -> "bN";
            case 'p' -> "bP";
            default -> null; // empty square
        };
    }

    private static String unicodeGlyphFor(char piece) {
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
            default -> "";
        };
    }
}