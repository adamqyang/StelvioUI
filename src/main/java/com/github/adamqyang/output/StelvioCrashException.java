package com.github.adamqyang.output;

/**
 * Thrown when problems_out.txt shows Stelvio crashed rather than producing
 * normal solve output - a raw, unhandled Java stack trace dumped into the
 * file instead of "Found solutions:" and a verdict. Kept distinct from a
 * generic "this doesn't look like a Stelvio output file" failure so
 * callers can show something specific and actionable instead.
 */
public class StelvioCrashException extends RuntimeException {

    public enum Kind {
        OUT_OF_MEMORY,
        UNKNOWN
    }

    private final Kind kind;

    public StelvioCrashException(Kind kind, String message) {
        super(message);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }
}
