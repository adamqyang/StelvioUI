package com.github.adamqyang.output;

import java.util.Arrays;

/**
 * Dev-only: parses the two real full problems_out.txt samples shared during
 * development (one normal solve, one cooked) and prints the result, to
 * confirm verdict/count/solving-time extraction and that the strategy-dump
 * block - which appears in a DIFFERENT position relative to "Found
 * solutions:" in each sample - is correctly ignored either way.
 * <p>
 * Under src/test/java - never ships with the app.
 */
public class ProblemsOutParserSmokeTest {

    private static final String NORMAL_SOLVE = """
             /* ****************************************************************************************** */
             /* Stelvio 4.5 Copyright 2023-26 Reto Aschwanden                                              */
             /* ****************************************************************************************** */
               Starting at: 15:37:54; 26.08.2026   Position [1n1q1bnr/2p1pppp/p2p2q1/1r3kBB/Q1N4K/PPPPPPPP/8/5bNR]
               **************************
               * .  s  .  q  .  b  s  r *
               * .  .  p  .  p  p  p  p *
               * p  .  .  p  .  .  q  . *
               * .  r  .  .  .  k  B  B *
               * Q  .  S  .  .  .  .  K *
               * P  P  P  P  P  P  P  P *
               * .  .  .  .  .  .  .  . *
               * .  .  .  .  .  b  S  R *
               **************************
               17.5              (15, 16)
               Found solutions: 
               1.Nb1a3 b5 2.Na3c4 b4 3.a3 b3 4.Ra1a2 bxa2 5.c3 a1=Q 6.Qd1a4 Qa1b1 7.b3 Qb1g6 8.d3 a6 9.Bc1g5 Ra8a7 10.e3 Ra7b7 11.Bf1e2 Rb7b5 12.Be2h5 d6 13.f3 Bc8h3 14.Ke1f2 Ke8d7 15.Kf2g3 Kd7e6 16.Kg3h4 Ke6f5 17.g3 Bh3f1 18.h3
            1: (0+0)
             3 Ke1-h4                                                    3 Ke8-f5
             1 Qd1-a4                                                    0 Qd8
             1 Ra1-a2 (Pb7)                                              3 Ra8-b5
             0 Rh1                                                       0 Rh8
             1 Bc1-g5                                                    2 Bc8-f1
             2 Bf1-h5                                                    0 Bf8
             2 Sb1-c4                                                    0 Sb8
             0 Sg1                                                       0 Sg8
             1 Pa2-a3                                                    1 Pa7-a6
             1 Pb2-b3                                                    7 Pb7xRa1|a2-a1=Q-g6
             1 Pc2-c3                                                    0 Pc7
             1 Pd2-d3                                                    1 Pd7-d6
             1 Pe2-e3                                                    0 Pe7
             1 Pf2-f3                                                    0 Pf7
             1 Pg2-g3                                                    0 Pg7
             1 Ph2-h3                                                    0 Ph7
               Found 1 solution. The problem is correct.
               Solving time: 00:00:01 seconds.
            """;

    private static final String COOKED = """
             /* ****************************************************************************************** */
             /* Stelvio 4.5 Copyright 2023-26 Reto Aschwanden                                              */
             /* ****************************************************************************************** */
               Starting at: 23:23:41; 25.08.2026   Position [1n1q1bnr/2p1pppp/p2p2q1/1r3kBB/Q1N4K/PPPPPPPP/8/5bNR]
               **************************
               * .  s  .  q  .  b  s  r *
               * .  .  p  .  p  p  p  p *
               * p  .  .  p  .  .  q  . *
               * .  r  .  .  .  k  B  B *
               * Q  .  S  .  .  .  .  K *
               * P  P  P  P  P  P  P  P *
               * .  .  .  .  .  .  .  . *
               * .  .  .  .  .  b  S  R *
               **************************
               18.0              (15, 16)
            1: (0+1)
             3 Ke1-h4                                                    3 Ke8-f5
             1 Qd1-a4                                                    0 Qd8
             1 Ra1-a2 (Pb7)                                              3 Ra8-b5
             0 Rh1                                                       0 Rh8
             1 Bc1-g5                                                    2 Bc8-f1
             2 Bf1-h5                                                    0 Bf8
             2 Sb1-c4                                                    0 Sb8
             0 Sg1                                                       0 Sg8
             1 Pa2-a3                                                    1 Pa7-a6
             1 Pb2-b3                                                    7 Pb7xRa1|a2-a1=Q-g6
             1 Pc2-c3                                                    0 Pc7
             1 Pd2-d3                                                    1 Pd7-d6
             1 Pe2-e3                                                    0 Pe7
             1 Pf2-f3                                                    0 Pf7
             1 Pg2-g3                                                    0 Pg7
             1 Ph2-h3                                                    0 Ph7
               Found solutions: 
               1.Nb1a3 b5 2.Na3c4 b4 3.a3 b3 4.Ra1a2 bxa2 5.c3 a1=Q 6.Qd1a4 Qa1b1 7.b3 Qb1e4 8.d3 a6 9.Bc1g5 Ra8a7 10.e3 Ra7b7 11.Bf1e2 Rb7b5 12.Be2h5 Qe4g6 13.f3 d6 14.Ke1f2 Bc8h3 15.Kf2g3 Ke8d7 16.Kg3h4 Kd7e6 17.g3 Bh3f1 18.h3 Ke6f5
               1.Nb1a3 b5 2.Na3c4 b4 3.a3 b3 4.Ra1a2 bxa2 5.c3 a1=Q 6.Qd1a4 Qa1b1 7.b3 Qb1e4 8.d3 a6 9.Bc1g5 Ra8a7 10.e3 Ra7b7 11.Bf1e2 Rb7b5 12.Be2h5 d6 13.f3 Bc8h3 14.Ke1f2 Ke8d7 15.Kf2g3 Qe4g6 16.Kg3h4 Kd7e6 17.g3 Bh3f1 18.h3 Ke6f5
               /* ****************************************************************************************** */ 
               The problem is cooked.
               Solving time: 00:00:01 seconds.
            """;

    public static void main(String[] args) {
        System.out.println("=== Normal solve ===");
        printResult(ProblemsOutParser.parse(Arrays.asList(NORMAL_SOLVE.split("\n"))));

        System.out.println();
        System.out.println("=== Cooked ===");
        printResult(ProblemsOutParser.parse(Arrays.asList(COOKED.split("\n"))));
    }

    private static void printResult(SolveResult result) {
        System.out.println("Verdict: " + result.verdict());
        System.out.println("Solution count: " + result.solutionCount());
        System.out.println("Solving time: " + result.solvingTime());
        System.out.println("Solutions parsed: " + result.solutions().size());
        for (SolveResult.Solution solution : result.solutions()) {
            System.out.println("  " + solution.moves().size() + " moves: " + solution.rawText());
        }
    }
}
