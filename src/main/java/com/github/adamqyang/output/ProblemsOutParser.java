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
    private static final Pattern NO_SOLUTION_VERDICT = Pattern.compile("The problem has no solution\\.");
    private static final Pattern SOLVING_TIME = Pattern.compile("Solving time:\\s*(.+)");
    // Matches a Java stack trace frame after trimming, e.g.
    // "at ch.stelvio.b.b.a.a.c.<init>(Unknown Source)" - Stelvio's own
    // output is never shaped like this, so its presence is a reliable
    // signal that we're looking at a raw, unhandled crash dump instead.
    private static final Pattern STACK_TRACE_FRAME = Pattern.compile("^at\\s+\\S+\\(.*\\)$");

    private ProblemsOutParser() {
    }

    public static SolveResult parse(Path outputFile) throws IOException {
        return parse(Files.readAllLines(outputFile));
    }

    static SolveResult parse(List<String> lines) {
        int markerIndex = indexOfMarker(lines);
        boolean hasMarker = markerIndex >= 0;
        int scanFrom = hasMarker ? markerIndex + 1 : 0;

        List<SolveResult.Solution> solutions = new ArrayList<>();
        SolveResult.Verdict verdict = null;
        Integer solutionCount = null;
        String solvingTime = null;

        for (int i = scanFrom; i < lines.size(); i++) {
            String trimmed = lines.get(i).trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            // Only treated as a move once we're confirmed past a genuine
            // "Found solutions:" marker - without that guard, a line like
            // "32.0              (15, 15)" (the pre-solve move-count/piece-
            // count progress line, seen in every real sample BEFORE any
            // marker) would falsely match this digit-dot pattern too, and
            // MoveNotationParser would then choke trying to parse "(15,"
            // as a move token.
            if (hasMarker && SOLUTION_LINE.matcher(trimmed).matches()) {
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

            if (NO_SOLUTION_VERDICT.matcher(trimmed).find()) {
                // A genuine, valid outcome - no "Found solutions:" marker
                // ever appears in this case (confirmed from a real sample),
                // so this can only ever be found via the whole-file scan.
                verdict = SolveResult.Verdict.NO_SOLUTION;
                continue;
            }

            Matcher timeMatcher = SOLVING_TIME.matcher(trimmed);
            if (timeMatcher.find()) {
                solvingTime = timeMatcher.group(1).trim();
            }
        }

        if (verdict == null) {
            // No recognizable verdict anywhere in the file - last resort,
            // check whether this looks like a crash instead of just an
            // unrecognized file.
            StelvioCrashException crash = detectCrash(lines);
            if (crash != null) {
                throw crash;
            }
            throw new IllegalArgumentException(
                    "Could not find a recognizable verdict in the output file - "
                            + "is this a genuine Stelvio problems_out.txt?");
        }

        // "Found N solutions." is sometimes absent entirely (confirmed real
        // behavior for cooked results, and always true for NO_SOLUTION) -
        // fall back to what we actually parsed, which is trustworthy either way.
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

    /**
     * Scans for a raw stack trace (Stelvio crashed before ever reaching
     * "Found solutions:"), returning null if none is found so the caller
     * falls back to the generic "unrecognized file" error instead.
     */
    private static StelvioCrashException detectCrash(List<String> lines) {
        boolean hasStackFrame = false;
        String headline = null;

        for (String rawLine : lines) {
            String trimmed = rawLine.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (STACK_TRACE_FRAME.matcher(trimmed).matches()) {
                hasStackFrame = true;
                continue;
            }
            if (headline == null && (trimmed.contains("Error") || trimmed.contains("Exception"))) {
                headline = trimmed;
            }
        }

        if (!hasStackFrame) {
            return null;
        }
        if (headline != null && headline.contains("OutOfMemoryError")) {
            return new StelvioCrashException(StelvioCrashException.Kind.OUT_OF_MEMORY,
                    "Stelvio ran out of memory while solving this problem. Try increasing the RAM "
                            + "setting and running again.");
        }
        return new StelvioCrashException(StelvioCrashException.Kind.UNKNOWN,
                "Stelvio encountered an internal error while solving this problem"
                        + (headline != null ? " (" + headline + ")" : "") + ". You may want to report "
                        + "this to Stelvio's author with the problems_out.txt file attached.");
    }
}