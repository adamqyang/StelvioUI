package com.github.adamqyang.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes a single problem into problems.txt. Stelvio's docs note the file can
 * hold multiple problems solved in succession — we're deliberately writing
 * just one for now, matching the current single-position UI flow. Batch
 * support can be layered on later without changing this file's shape.
 */
public final class ProblemsFileWriter {

    private ProblemsFileWriter() {
    }

    public static void write(Path installFolder, SolveRequest request) throws IOException {
        StringBuilder content = new StringBuilder();
        content.append(request.fen().trim()).append('\n');
        content.append(request.moveCount().halfMoves()).append('\n');
        if (request.hasStrategyConditions()) {
            content.append("StrategyConditions: ").append(request.strategyConditions().trim()).append('\n');
        }
        Files.writeString(installFolder.resolve("problems.txt"), content.toString());
    }
}
