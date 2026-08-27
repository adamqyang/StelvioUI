package com.github.adamqyang.chess;

/** A board square, using Position's internal indexing (rank 0 = rank 8, file 0 = the a-file). */
public record Square(int rank, int file) {

    public static Square parse(String algebraic) {
        if (algebraic == null || algebraic.length() != 2) {
            throw new IllegalArgumentException("Invalid square: \"" + algebraic + "\"");
        }
        char fileChar = algebraic.charAt(0);
        char rankChar = algebraic.charAt(1);
        if (fileChar < 'a' || fileChar > 'h' || rankChar < '1' || rankChar > '8') {
            throw new IllegalArgumentException("Invalid square: \"" + algebraic + "\"");
        }
        int file = fileChar - 'a';
        int rank = 8 - (rankChar - '0'); // rank '8' -> index 0, rank '1' -> index 7
        return new Square(rank, file);
    }

    public String toAlgebraic() {
        char fileChar = (char) ('a' + file);
        char rankChar = (char) ('0' + (8 - rank));
        return "" + fileChar + rankChar;
    }
}
