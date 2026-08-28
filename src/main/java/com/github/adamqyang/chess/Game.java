package com.github.adamqyang.chess;

import java.util.ArrayList;
import java.util.List;

/**
 * Replays a list of (board-unaware) Moves onto a starting Position,
 * resolving everything the notation itself left unspecified: pawn origins
 * (both push and capture), en passant, and concrete castling squares.
 * <p>
 * Color-to-move is inferred from each move's index (0-based, White plays
 * even indices), matching Stelvio's own numbered-pair convention and
 * standard chess alternation.
 * <p>
 * All positions are computed eagerly in the constructor - move lists here
 * are short (tens of moves at most), so there's no real cost to it, and it
 * keeps positionAfter() trivial.
 */
public final class Game {

    /**
     * The standard chess starting position, in Stelvio's piece-placement-only
     * FEN form. Solution replay always starts here, never from the diagram
     * FEN - that's the entire premise of a proof game: proving a path FROM
     * the standard start TO the diagram position.
     */
    public static final String STANDARD_STARTING_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";

    private final List<Move> moves;
    private final List<Position> positions; // positions.get(0) = start; positions.get(i) = after moves.get(i-1)

    public Game(Position startingPosition, List<Move> moves) {
        this.moves = List.copyOf(moves);
        this.positions = new ArrayList<>(moves.size() + 1);
        positions.add(startingPosition);

        char[][] working = startingPosition.copyBoard();
        for (int i = 0; i < this.moves.size(); i++) {
            boolean whiteToMove = i % 2 == 0;
            applyMove(working, this.moves.get(i), whiteToMove);
            positions.add(Position.fromBoard(cloneBoard(working)));
        }
    }

    /** Number of moves in this game (not counting the starting position). */
    public int moveCount() {
        return moves.size();
    }

    public Move moveAt(int index) {
        return moves.get(index);
    }

    /** The position before any moves have been played. */
    public Position startingPosition() {
        return positions.get(0);
    }

    /** The position after {@code moveIndex} moves have been played (0 = starting position). */
    public Position positionAfter(int moveIndex) {
        return positions.get(moveIndex);
    }

    // --- move application ---------------------------------------------------

    private static void applyMove(char[][] board, Move move, boolean whiteToMove) {
        switch (move.type()) {
            case KINGSIDE_CASTLE -> applyCastle(board, whiteToMove, true);
            case QUEENSIDE_CASTLE -> applyCastle(board, whiteToMove, false);
            case NORMAL -> applyNormalMove(board, move, whiteToMove);
        }
    }

    private static void applyCastle(char[][] board, boolean whiteToMove, boolean kingside) {
        int rank = whiteToMove ? 7 : 0; // rank 1 for white, rank 8 for black
        int kingFromFile = 4; // e-file
        int kingToFile = kingside ? 6 : 2; // g or c
        int rookFromFile = kingside ? 7 : 0; // h or a
        int rookToFile = kingside ? 5 : 3; // f or d

        char king = whiteToMove ? 'K' : 'k';
        char rook = whiteToMove ? 'R' : 'r';

        board[rank][kingFromFile] = Position.EMPTY;
        board[rank][rookFromFile] = Position.EMPTY;
        board[rank][kingToFile] = king;
        board[rank][rookToFile] = rook;
    }

    private static void applyNormalMove(char[][] board, Move move, boolean whiteToMove) {
        Square destination = Square.parse(move.destination());
        Square origin = resolveOrigin(board, move, whiteToMove, destination);

        // En passant: a pawn capture where the destination square is empty
        // in the pre-move position means the captured pawn isn't ON the
        // destination - it's beside the origin, on the destination's file.
        boolean isEnPassant = move.piece() == 'P' && move.isCapture()
                && board[destination.rank()][destination.file()] == Position.EMPTY;
        if (isEnPassant) {
            board[origin.rank()][destination.file()] = Position.EMPTY;
        }

        board[origin.rank()][origin.file()] = Position.EMPTY;

        char finalPieceLetter = move.promotionPiece() != null ? move.promotionPiece() : move.piece();
        board[destination.rank()][destination.file()] = resolvePieceChar(finalPieceLetter, whiteToMove);
    }

    /** Resolves a Move's (possibly partial) originHint into a concrete Square, using board state where needed. */
    private static Square resolveOrigin(char[][] board, Move move, boolean whiteToMove, Square destination) {
        String hint = move.originHint();

        if (hint.length() == 2) {
            return Square.parse(hint); // piece move - already fully specified
        }

        if (hint.length() == 1) {
            // Pawn capture: file given, rank is always one behind the destination.
            int file = hint.charAt(0) - 'a';
            int step = whiteToMove ? 1 : -1;
            return new Square(destination.rank() + step, file);
        }

        // Pawn push: search backward up to 2 squares on the destination's file.
        char pawn = whiteToMove ? 'P' : 'p';
        int file = destination.file();
        int step = whiteToMove ? 1 : -1;
        for (int distance = 1; distance <= 2; distance++) {
            int rank = destination.rank() + step * distance;
            if (rank < 0 || rank >= Position.SIZE) {
                break;
            }
            if (board[rank][file] == pawn) {
                return new Square(rank, file);
            }
        }
        throw new IllegalStateException(
                "Could not find the pawn that moved to " + destination.toAlgebraic()
                        + " (searched up to 2 squares back on the " + (char) ('a' + file) + "-file).");
    }

    private static char resolvePieceChar(char pieceLetter, boolean whiteToMove) {
        return whiteToMove ? Character.toUpperCase(pieceLetter) : Character.toLowerCase(pieceLetter);
    }

    private static char[][] cloneBoard(char[][] board) {
        char[][] copy = new char[board.length][];
        for (int i = 0; i < board.length; i++) {
            copy[i] = board[i].clone();
        }
        return copy;
    }
}