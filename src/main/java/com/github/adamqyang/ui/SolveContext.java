package com.github.adamqyang.ui;

import java.nio.file.Path;

/**
 * Bundles what ResultsScreenController needs beyond the parsed SolveResult
 * itself - the output file's path (for "Open output file") and the
 * original diagram FEN the user typed on the input tab (for display
 * alongside the solutions).
 */
public record SolveContext(Path outputFile, String originalFen) {
}
