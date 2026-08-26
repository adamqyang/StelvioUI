package com.github.adamqyang.chess;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses Stelvio's solution-list move notation (the fully-qualified format
 * after "Found solutions:" in problems_out.txt) into {@link Move}s. Only
 * ever operates on an already-isolated solution line - has no awareness of
 * the surrounding file structure (header, board diagram, strategy dumps,
 * verdict), which is output/'s job to isolate first.
 */
public final class MoveNotationParser {

    private static final Pattern MOVE_NUMBER = Pattern.compile("^\\d+\\.");

    private static final Pattern PAWN_PUSH =
            Pattern.compile("([a-h][1-8])(?:=([QRBN]))?(\\+)?");
    private static final Pattern PAWN_CAPTURE =
            Pattern.compile("([a-h])x([a-h][1-8])(?:=([QRBN]))?(\\+)?");
    private static final Pattern PIECE_QUIET =
            Pattern.compile("([KQRBN])([a-h][1-8])([a-h][1-8])(\\+)?");
    private static final Pattern PIECE_CAPTURE =
            Pattern.compile("([KQRBN])([a-h][1-8])x([a-h][1-8])(\\+)?");

    private MoveNotationParser() {
    }

    /**
     * Parses a whole solution line (e.g. "1.Nb1c3 c6 2.Nc3d5 Qd8c7 3...")
     * into an ordered list of moves. Move-number prefixes ("1.", "17.") are
     * stripped from whichever token they're attached to (only White's
     * moves carry one); tokens without a prefix pass through unchanged.
     */
    public static List<Move> parseMoveList(String line) {
        List<Move> moves = new ArrayList<>();
        for (String token : line.trim().split("\\s+")) {
            if (token.isBlank()) {
                continue;
            }
            String stripped = MOVE_NUMBER.matcher(token).replaceFirst("");
            moves.add(parse(stripped));
        }
        return moves;
    }

    /** Parses a single move token, e.g. "Nb1c3", "hxg3", "a1=Q", "O-O", "Bh5xf7+". */
    public static Move parse(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Empty move token.");
        }
        String trimmed = token.trim();

        if (trimmed.startsWith("O-O")) {
            boolean isCheck = trimmed.endsWith("+");
            String core = isCheck ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
            if (core.equals("O-O")) {
                return new Move(Move.MoveType.KINGSIDE_CASTLE, null, "", null, false, null, isCheck);
            }
            if (core.equals("O-O-O")) {
                return new Move(Move.MoveType.QUEENSIDE_CASTLE, null, "", null, false, null, isCheck);
            }
            throw new IllegalArgumentException("Unrecognized castling notation: \"" + trimmed + "\"");
        }

        Matcher m = PIECE_CAPTURE.matcher(trimmed);
        if (m.matches()) {
            return new Move(Move.MoveType.NORMAL, m.group(1).charAt(0), m.group(2), m.group(3),
                    true, null, m.group(4) != null);
        }

        m = PIECE_QUIET.matcher(trimmed);
        if (m.matches()) {
            return new Move(Move.MoveType.NORMAL, m.group(1).charAt(0), m.group(2), m.group(3),
                    false, null, m.group(4) != null);
        }

        m = PAWN_CAPTURE.matcher(trimmed);
        if (m.matches()) {
            Character promo = m.group(3) != null ? m.group(3).charAt(0) : null;
            return new Move(Move.MoveType.NORMAL, 'P', m.group(1), m.group(2),
                    true, promo, m.group(4) != null);
        }

        m = PAWN_PUSH.matcher(trimmed);
        if (m.matches()) {
            Character promo = m.group(2) != null ? m.group(2).charAt(0) : null;
            return new Move(Move.MoveType.NORMAL, 'P', "", m.group(1),
                    false, promo, m.group(3) != null);
        }

        throw new IllegalArgumentException("Unrecognized move notation: \"" + trimmed + "\"");
    }
}
