package com.github.adamqyang.output;

import java.util.List;

import com.github.adamqyang.chess.Move;

/**
 * A parsed problems_out.txt for a single problem. This tool currently only
 * ever writes one problem per file (ProblemsFileWriter), so multi-problem
 * batch files aren't handled here yet - a future extension, not a current
 * gap.
 */
public record SolveResult(
        List<Solution> solutions,
        Verdict verdict,
        int solutionCount,
        String solvingTime // raw text, e.g. "00:00:01 seconds"; null if the file didn't include it
) {
    public enum Verdict {
        CORRECT, COOKED
    }

    /**
     * One solution/cook line. rawText is Stelvio's own notation as-is -
     * already suitable for copy/paste per the docs' own note that it's
     * "sufficient for use in tools like lichess.org" - and moves is the
     * same text parsed for board replay.
     */
    public record Solution(String rawText, List<Move> moves) {
    }
}
