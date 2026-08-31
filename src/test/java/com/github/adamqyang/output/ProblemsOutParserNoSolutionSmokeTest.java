package com.github.adamqyang.output;

import java.util.Arrays;

/**
 * Dev-only: parses a trimmed version of the real "no solution" sample
 * shared during development. Trimmed to 2 strategy blocks rather than the
 * real file's 50 - every block is structurally identical and ignored the
 * same way regardless of count, so this exercises the exact same code
 * paths without embedding ~900 lines of repetition.
 * <p>
 * This specifically exercises the case that motivated gating solution-line
 * detection on a confirmed marker: this file has NO "Found solutions:"
 * marker at all, and its "32.0              (15, 15)" progress line would
 * otherwise false-positive-match the solution-line pattern.
 * <p>
 * Under src/test/java - never ships with the app.
 */
public class ProblemsOutParserNoSolutionSmokeTest {

    private static final String NO_SOLUTION_SAMPLE = """
             /* ****************************************************************************************** */
             /* Stelvio 4.5 Copyright 2023-26 Reto Aschwanden                                              */
             /* ****************************************************************************************** */


               Starting at: 23:56:46; 29.08.2026   Position [B4b2/r2ppp2/n1prRrRn/P1k4p/2NR2Q1/1bR2N1r/KBPPPP2/7q]

               **************************
               * B  .  .  .  .  b  .  . *
               * r  .  .  p  p  p  .  . *
               * s  .  p  r  R  r  R  s *
               * P  .  k  .  .  .  .  p *
               * .  .  S  R  .  .  Q  . *
               * .  b  R  .  .  S  .  r *
               * K  B  P  P  P  P  .  . *
               * .  .  .  .  .  .  .  q *
               **************************
               32.0              (15, 15)


            1: (0+0)
             3 Kc1-c1-a2                                                 4 Ke8-c5
             3 Qd1-g4                                                    2 Qd8-h1
             3 Rd1-d1-d4                                                 1 Ra8-a7
             2 Rh1-e6                                                    2 Rh8-f6


            2: (0+0)
             3 Kc1-c1-a2                                                 4 Ke8-c5
             3 Qd1-g4                                                    2 Qd8-h1
             3 Rd1-d1-d4                                                 1 Ra8-a7
             2 Rh1-e6                                                    2 Rh8-f6


               The problem has no solution.

               Solving time: 00:00:14 seconds.

            """;

    public static void main(String[] args) {
        SolveResult result = ProblemsOutParser.parse(Arrays.asList(NO_SOLUTION_SAMPLE.split("\n")));

        System.out.println("Verdict: " + result.verdict());
        System.out.println("Solution count: " + result.solutionCount());
        System.out.println("Solving time: " + result.solvingTime());
        System.out.println("Solutions parsed: " + result.solutions().size());

        boolean pass = result.verdict() == SolveResult.Verdict.NO_SOLUTION
                && result.solutionCount() == 0
                && result.solutions().isEmpty()
                && "00:00:14 seconds.".equals(result.solvingTime());
        System.out.println(pass ? "PASS" : "FAIL");
    }
}