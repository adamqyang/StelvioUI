package com.github.adamqyang.chess;

import java.util.List;

/**
 * Dev-only: parses every real solution line shared during development and
 * prints the result, to eyeball against the known-correct source text
 * before this gets used for board replay. Under src/test/java - never
 * ships with the app.
 */
public class MoveNotationParserSmokeTest {
    public static void main(String[] args) {
        List<String> sampleLines = List.of(
                // Original single-solution sample, includes a check suffix
                "1.Nb1c3 c6 2.Nc3d5 Qd8c7 3.c3 Qc7g3 4.hxg3 a5 5.Rh1h4 Ra8a6 6.Rh4b4 Ra6b6 7.e4 Rb6b5 8.Bf1e2 Rb5c5 9.Be2h5 Rc5c4 10.Qd1f3 Rc4d4 11.Ke1d1 Rd4d3 12.Kd1c2 Rd3e3 13.Kc2b3 Re3e2 14.Kb3a4 Re2xf2 15.b3 Rf2f1 16.Bc1a3 Rf1xa1 17.Bh5xf7+",
                // Multi-solution sample, includes castling both sides
                "1.b3 h5 2.Bc1b2 Ng8h6 3.Bb2f6 gxf6 4.Nb1c3 Bf8g7 5.Nc3e4 O-O 6.c3 Kg8h7 7.Qd1c2 Rf8h8 8.O-O-O Qd8g8 9.Kc1b2 Bg7f8 10.Rd1a1",
                // Promotion sample
                "1.Nb1a3 b5 2.Na3c4 b4 3.a3 b3 4.Ra1a2 bxa2 5.c3 a1=Q 6.Qd1a4 Qa1b1 7.b3 Qb1g6 8.d3 a6 9.Bc1g5 Ra8a7 10.e3 Ra7b7 11.Bf1e2 Rb7b5 12.Be2h5 d6 13.f3 Bc8h3 14.Ke1f2 Ke8d7 15.Kf2g3 Kd7e6 16.Kg3h4 Ke6f5 17.g3 Bh3f1 18.h3",
                // Cook sample, second branch (diverges after move 7)
                "1.Nb1a3 b5 2.Na3c4 b4 3.a3 b3 4.Ra1a2 bxa2 5.c3 a1=Q 6.Qd1a4 Qa1b1 7.b3 Qb1e4 8.d3 a6 9.Bc1g5 Ra8a7 10.e3 Ra7b7 11.Bf1e2 Rb7b5 12.Be2h5 d6 13.f3 Bc8h3 14.Ke1f2 Ke8d7 15.Kf2g3 Qe4g6 16.Kg3h4 Kd7e6 17.g3 Bh3f1 18.h3 Ke6f5"
        );

        for (String line : sampleLines) {
            System.out.println("Line: " + line);
            List<Move> moves = MoveNotationParser.parseMoveList(line);
            for (Move move : moves) {
                System.out.println("  " + move);
            }
            System.out.println();
        }
    }
}
