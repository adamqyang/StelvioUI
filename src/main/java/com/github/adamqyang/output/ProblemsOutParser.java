package com.github.adamqyang.output;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.adamqyang.chess.Move;
import com.github.adamqyang.chess.MoveNotationParser;

/**
 * Parses problems_out.txt into a SolveResult.
 * <p>
 * Deliberately locates content by stable text markers rather than fixed
 * line positions. Real output shows a strategy-dump block (from
 * printStrategies=true) that can appear either BEFORE "Found solutions:"
 * or AFTER the move list but BEFORE the verdict, depending on the run -
 * position-based parsing isn't reliable here. Everything not explicitly
 * recognized (headers, the FEN/position line, the ASCII board, strategy
 * dumps, separator lines) is silently ignored rather than causing errors.
 */
public final class ProblemsOutParser {

    private static final String FOUND_SOLUTIONS_MARKER = "Found solutions:";
    private static final Pattern SOLUTION_LINE = Pattern.compile("^\\d+\\..*");
    private static final Pattern SOLUTION_COUNT =
            Pattern.compile("Found (\\d+) solutions?\\. The problem is correct\\.");
    private static final Pattern COOKED_VERDICT = Pattern.compile("The problem is cooked\\.");
    private static final Pattern SOLVING_TIME = Pattern.compile("Solving time:\\s*(.+)");

    private ProblemsOutParser() {
    }

    public static SolveResult parse(Path outputFile) throws IOException {
        return parse(Files.readAllLines(outputFile));
    }

    static SolveResult parse(List<String> lines) {
        int startIndex = indexOfMarker(lines);
        if (startIndex < 0) {
            throw new IllegalArgumentException(
                    "Could not find \"" + FOUND_SOLUTIONS_MARKER + "\" in the output file - "
                            + "is this a genuine Stelvio problems_out.txt?");
        }

        List<SolveResult.Solution> solutions = new ArrayList<>();
        SolveResult.Verdict verdict = null;
        Integer solutionCount = null;
        String solvingTime = null;

        for (int i = startIndex + 1; i < lines.size(); i++) {
            String trimmed = lines.get(i).trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            if (SOLUTION_LINE.matcher(trimmed).matches()) {
                List<Move> moves = MoveNotationParser.parseMoveList(trimmed);
                solutions.add(new SolveResult.Solution(trimmed, moves));
                continue;
            }

            Matcher countMatcher = SOLUTION_COUNT.matcher(trimmed);
            if (countMatcher.find()) {
                solutionCount = Integer.parseInt(countMatcher.group(1));
                verdict = SolveResult.Verdict.CORRECT;
                continue;
            }

            if (COOKED_VERDICT.matcher(trimmed).find()) {
                verdict = SolveResult.Verdict.COOKED;
                continue;
            }

            Matcher timeMatcher = SOLVING_TIME.matcher(trimmed);
            if (timeMatcher.find()) {
                solvingTime = timeMatcher.group(1).trim();
            }
        }

        if (verdict == null) {
            throw new IllegalArgumentException(
                    "Could not find a verdict line (\"The problem is correct/cooked.\") in the output file.");
        }

        // "Found N solutions." is sometimes absent entirely (confirmed real
        // behavior for cooked results) - fall back to what we actually
        // parsed, which is trustworthy either way.
        int finalCount = solutionCount != null ? solutionCount : solutions.size();

        return new SolveResult(solutions, verdict, finalCount, solvingTime);
    }

    private static int indexOfMarker(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).trim().equals(FOUND_SOLUTIONS_MARKER)) {
                return i;
            }
        }
        return -1;
    }
}
