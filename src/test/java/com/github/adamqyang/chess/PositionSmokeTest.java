package com.github.adamqyang.chess;

/**
 * Dev-only: parses the FEN example from Stelvio's own documentation and
 * prints the resulting board, to eyeball against the known diagram by hand
 * before this ever touches JavaFX rendering. Under src/test/java - never
 * ships with the app.
 */
public class PositionSmokeTest {
    public static void main(String[] args) {
        Position position = Position.fromFen("1nbq4/ppk1p3/Rp5p/3npr2/R3P3/2br1B1P/PP2P2P/1NBQNK2");
        System.out.println(position);
    }
}
