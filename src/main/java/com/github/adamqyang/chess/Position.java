package com.github.adamqyang.chess;

/**
 * A chess position parsed from Stelvio's (piece-placement-only) FEN format.
 * No JavaFX dependency here by design - this is pure board-state modeling,
 * reused by both the FEN preview and (later) solution replay.
 */
public final class Position {

    public static final int SIZE = 8;

    static final char EMPTY = '.';

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

    /**
     * Returns a fresh, independent copy of the underlying board array.
     * Package-visible: used by Game to get a mutable starting point for
     * replay, without exposing board mutation outside chess/.
     */
    char[][] copyBoard() {
        char[][] copy = new char[SIZE][];
        for (int i = 0; i < SIZE; i++) {
            copy[i] = board[i].clone();
        }
        return copy;
    }

    /**
     * Constructs a Position directly from a board array, bypassing FEN
     * parsing entirely. Package-visible: used by Game, which produces
     * board arrays directly while replaying moves rather than ever having
     * a FEN string to parse.
     */
    static Position fromBoard(char[][] board) {
        return new Position(board);
    }

    /**
     * Serializes back to Stelvio's piece-placement-only FEN form - the
     * direct inverse of fromFen()'s parsing: run-length-encode consecutive
     * empty squares, join ranks with "/".
     */
    public String toFen() {
        StringBuilder sb = new StringBuilder();
        for (int rank = 0; rank < SIZE; rank++) {
            int emptyRun = 0;
            for (int file = 0; file < SIZE; file++) {
                char piece = board[rank][file];
                if (piece == EMPTY) {
                    emptyRun++;
                } else {
                    if (emptyRun > 0) {
                        sb.append(emptyRun);
                        emptyRun = 0;
                    }
                    sb.append(piece);
                }
            }
            if (emptyRun > 0) {
                sb.append(emptyRun);
            }
            if (rank < SIZE - 1) {
                sb.append('/');
            }
        }
        return sb.toString();
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