package com.github.adamqyang.chess;

import java.util.regex.Pattern;

/**
 * Lightweight structural validation for the FEN piece-placement strings
 * Stelvio expects. This deliberately checks FORMAT, not chess LEGALITY -
 * it won't catch things like "17 pawns on one side" or an impossible king
 * position. Full legality checking is out of scope until the rest of the
 * chess/ package (board modeling for the FEN preview and solution replay)
 * gets built, at which point this can be folded into that parser rather
 * than duplicated.
 * <p>
 * Note: Stelvio's FEN is piece-placement only - no active-color, castling,
 * en-passant, or move-clock fields, since whose move it is falls out of the
 * half-move count's parity instead. Every example in Stelvio's own
 * documentation uses this simplified form.
 */
public final class FenValidator {

    private static final Pattern RANK_PATTERN = Pattern.compile("[KQRBNSPkqrbnsp1-8]+");

    private FenValidator() {
    }

    /**
     * Throws IllegalArgumentException with a user-facing message if the FEN
     * isn't structurally valid. Returns normally if it looks reasonable.
     */
    public static void validate(String fen) {
        if (fen == null || fen.isBlank()) {
            throw new IllegalArgumentException("Enter a FEN for the diagram position.");
        }

        String[] ranks = fen.trim().split("/", -1);
        if (ranks.length != 8) {
            throw new IllegalArgumentException(
                    "FEN should have exactly 8 ranks separated by \"/\" (found " + ranks.length + ").");
        }

        int whiteKings = 0;
        int blackKings = 0;

        for (int i = 0; i < ranks.length; i++) {
            String rank = ranks[i];
            if (!RANK_PATTERN.matcher(rank).matches()) {
                throw new IllegalArgumentException(
                        "Rank " + (i + 1) + " (\"" + rank + "\") contains characters Stelvio doesn't recognize. "
                                + "Use piece letters (K Q R B N/S P, lowercase for black) and digits for empty squares.");
            }

            int squareCount = 0;
            for (char c : rank.toCharArray()) {
                if (Character.isDigit(c)) {
                    squareCount += Character.getNumericValue(c);
                } else {
                    squareCount += 1;
                    if (c == 'K') {
                        whiteKings++;
                    }
                    if (c == 'k') {
                        blackKings++;
                    }
                }
            }
            if (squareCount != 8) {
                throw new IllegalArgumentException(
                        "Rank " + (i + 1) + " (\"" + rank + "\") doesn't add up to 8 squares (got " + squareCount + ").");
            }
        }

        if (whiteKings != 1 || blackKings != 1) {
            throw new IllegalArgumentException(
                    "FEN must have exactly one white king (K) and one black king (k).");
        }
    }
}
