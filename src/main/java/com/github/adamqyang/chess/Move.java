package com.github.adamqyang.chess;

/**
 * A single parsed move token from Stelvio's solution notation (the
 * fully-qualified format after "Found solutions:" in problems_out.txt -
 * not the richer strategy/path notation shown elsewhere in that file,
 * which uses a different, more complex grammar we're not targeting).
 * <p>
 * This is deliberately board-unaware - it captures exactly what the text
 * says, no more. Some fields are only partially specified because
 * Stelvio's notation itself doesn't fully specify them:
 * <ul>
 *   <li>Pawn pushes give no origin at all (e.g. "c6") - the mover's actual
 *       starting square depends on board state (single vs. double step).</li>
 *   <li>Pawn captures give only the origin FILE (e.g. "hxg3") - the origin
 *       rank is always the destination rank +/-1 depending on color, but
 *       resolving that still requires knowing whose move it is.</li>
 *   <li>Piece moves give the FULL origin square (e.g. "Nb1c3").</li>
 *   <li>Castling ("O-O"/"O-O-O") gives no squares at all - which concrete
 *       squares move depends on whose turn it is.</li>
 * </ul>
 * Resolving these into concrete board squares (and handling en passant,
 * which is likewise indistinguishable from an ordinary pawn capture at the
 * notation level) is board-replay logic's job, not this class's.
 * <p>
 * Piece letters use N for knight (not S) throughout - matches Stelvio's
 * own solution-output convention. S-notation display is a possible future
 * option, not handled here.
 */
public record Move(
        MoveType type,
        Character piece,           // K/Q/R/B/N/P; null for castling
        String originHint,         // "" (pawn push/castling), 1 char (pawn capture file), or 2 chars (full origin square)
        String destination,        // full square, e.g. "c6"; null for castling
        boolean isCapture,
        Character promotionPiece,  // null unless a promotion
        boolean isCheck
) {
    public enum MoveType {
        NORMAL, KINGSIDE_CASTLE, QUEENSIDE_CASTLE
    }
}
