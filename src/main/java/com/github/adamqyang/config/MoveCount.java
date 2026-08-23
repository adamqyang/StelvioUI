package com.github.adamqyang.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Stelvio SPG length, expressed the way problem composers conventionally
 * write it (e.g. "23.0" or "31.5" moves), backed internally by the integer
 * half-move count that problems.txt actually expects on its second line.
 */
public final class MoveCount {

    private static final Pattern DISPLAY_PATTERN = Pattern.compile("(\\d+)(?:\\.(0|5))?");

    private final int halfMoves;

    private MoveCount(int halfMoves) {
        this.halfMoves = halfMoves;
    }

    /**
     * Parses text like "23", "23.0", or "23.5" into a MoveCount, enforcing the
     * limits Stelvio itself documents: no 0.5-move SPGs, and nothing over 60.0
     * moves.
     */
    public static MoveCount parse(String displayText) {
        if (displayText == null) {
            throw new IllegalArgumentException("Move count is required.");
        }
        Matcher matcher = DISPLAY_PATTERN.matcher(displayText.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Enter a move count like \"23\", \"23.0\", or \"23.5\".");
        }

        int wholeMoves = Integer.parseInt(matcher.group(1));
        boolean isHalfMove = "5".equals(matcher.group(2));
        int halfMoves = wholeMoves * 2 + (isHalfMove ? 1 : 0);

        if (halfMoves < 2) {
            throw new IllegalArgumentException(
                    "Stelvio does not support SPGs shorter than 1.0 moves (and refuses 0.5 moves specifically).");
        }
        if (halfMoves > 120) {
            throw new IllegalArgumentException("Stelvio does not support SPGs longer than 60.0 moves.");
        }

        return new MoveCount(halfMoves);
    }

    /** The raw half-move count, as written to problems.txt. */
    public int halfMoves() {
        return halfMoves;
    }

    /** The conventional "23.0" / "23.5" display form. */
    public String display() {
        int whole = halfMoves / 2;
        boolean isHalf = halfMoves % 2 != 0;
        return whole + (isHalf ? ".5" : ".0");
    }

    @Override
    public String toString() {
        return display();
    }
}
