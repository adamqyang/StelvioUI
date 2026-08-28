package com.github.adamqyang.chess;

import java.util.List;

/**
 * Dev-only: replays the real "correct" solution from the promotion/cook
 * sample problem, starting from the standard opening position, and checks
 * the resulting position square-by-square against the known-correct target
 * FEN from that same problem's own diagram. A strong end-to-end check -
 * pawn-origin inference (both push and capture), promotion, and general
 * move application are all exercised and cross-checked against real
 * ground truth in one pass.
 * <p>
 * Under src/test/java - never ships with the app.
 */
public class GameSmokeTest {

    public static void main(String[] args) {
        String targetFen = "1n1q1bnr/2p1pppp/p2p2q1/1r3kBB/Q1N4K/PPPPPPPP/8/5bNR";
        String solutionLine =
                "1.Nb1a3 b5 2.Na3c4 b4 3.a3 b3 4.Ra1a2 bxa2 5.c3 a1=Q 6.Qd1a4 Qa1b1 7.b3 Qb1g6 "
                        + "8.d3 a6 9.Bc1g5 Ra8a7 10.e3 Ra7b7 11.Bf1e2 Rb7b5 12.Be2h5 d6 13.f3 Bc8h3 "
                        + "14.Ke1f2 Ke8d7 15.Kf2g3 Kd7e6 16.Kg3h4 Ke6f5 17.g3 Bh3f1 18.h3";

        List<Move> moves = MoveNotationParser.parseMoveList(solutionLine);
        Game game = new Game(Position.fromFen(Game.STANDARD_STARTING_FEN), moves);
        Position result = game.positionAfter(game.moveCount());
        Position target = Position.fromFen(targetFen);

        System.out.println("Replayed position:");
        System.out.println(result);
        System.out.println("Target position:");
        System.out.println(target);

        boolean matches = true;
        for (int rank = 0; rank < Position.SIZE; rank++) {
            for (int file = 0; file < Position.SIZE; file++) {
                char actual = result.pieceAt(rank, file);
                char expected = target.pieceAt(rank, file);
                if (actual != expected) {
                    matches = false;
                    System.out.println("MISMATCH at rank=" + rank + " file=" + file
                            + ": replayed='" + actual + "' target='" + expected + "'");
                }
            }
        }
        System.out.println(matches
                ? "MATCH: replay reached the exact target position."
                : "MISMATCH FOUND - see above.");
    }
}