package com.github.adamqyang.chess;

/**
 * A chess position parsed from Stelvio's (piece-placement-only) FEN format.
 * No JavaFX dependency here by design - this is pure board-state modeling,
 * reused by both the FEN preview and (later) solution replay.
 */
public final class Position {

    public static final int SIZE = 8;

    private static final char EMPTY = '.';

    // board[rank][file]: rank 0 = rank 8 (top of a standard diagram), file 0 = the a-file.
    private final char[][] board;

    private Position(char[][] board) {
        this.board = board;
    }

    /**
     * Parses a FEN piece-placement string into a Position. Delegates to
     * FenValidator first, so this and the input screen's validation can
     * never disagree about what counts as well-formed.
     */
    public static Position fromFen(String fen) {
        FenValidator.validate(fen);

        char[][] board = new char[SIZE][SIZE];
        String[] ranks = fen.trim().split("/", -1);

        for (int rank = 0; rank < SIZE; rank++) {
            int file = 0;
            for (char c : ranks[rank].toCharArray()) {
                if (Character.isDigit(c)) {
                    int emptySquares = Character.getNumericValue(c);
                    for (int i = 0; i < emptySquares; i++) {
                        board[rank][file++] = EMPTY;
                    }
                } else {
                    board[rank][file++] = c;
                }
            }
        }

        return new Position(board);
    }

    /**
     * The piece character at the given square, or a non-piece placeholder
     * character if empty. rank is 0 (rank 8) through 7 (rank 1); file is 0
     * (the a-file) through 7 (the h-file).
     */
    public char pieceAt(int rank, int file) {
        return board[rank][file];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int rank = 0; rank < SIZE; rank++) {
            for (int file = 0; file < SIZE; file++) {
                sb.append(board[rank][file]).append(' ');
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
