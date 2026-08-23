package com.github.adamqyang.install;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks whether a candidate folder looks like a genuine, fully-extracted
 * Stelvio installation, and if so, extracts the version string.
 * <p>
 * We deliberately check for the *presence* of expected files/folders rather
 * than inspecting their contents — that's enough to catch "wrong folder" or
 * "partially extracted zip" cases without being fragile to version-specific
 * differences inside those files.
 */
public final class InstallationValidator {

    // Matches folder names like "stelvio4.5" and captures "4.5". Requires at
    // least one dot, so a version-less folder name (e.g. "stelvio") correctly
    // falls through to "unknown" rather than matching nothing sensible.
    private static final Pattern VERSION_PATTERN =
            Pattern.compile("stelvio(\\d+(?:\\.\\d+)+)", Pattern.CASE_INSENSITIVE);

    private InstallationValidator() {
        // Utility class, not meant to be instantiated.
    }

    public static ValidationResult validate(Path folder) {
        List<String> missing = new ArrayList<>();

        if (folder == null || !Files.isDirectory(folder)) {
            missing.add("folder does not exist");
            return new ValidationResult.Invalid(missing);
        }

        if (findMatch(folder.resolve("bin"), "stelvio*.jar") == null) {
            missing.add("bin/stelvio*.jar");
        }
        if (!Files.isRegularFile(folder.resolve("stelvioUI.ini"))) {
            missing.add("stelvioUI.ini");
        }
        if (!Files.isRegularFile(folder.resolve("problems.txt"))) {
            missing.add("problems.txt");
        }
        if (findMatch(folder, "stelvio*.bat") == null) {
            missing.add("stelvio*.bat");
        }
        if (findMatch(folder, "stelvio*.sh") == null) {
            missing.add("stelvio*.sh");
        }

        if (!missing.isEmpty()) {
            return new ValidationResult.Invalid(missing);
        }

        String version = extractVersionFromFolderName(folder.getFileName().toString());
        return new ValidationResult.Valid(
                new StelvioInstallation(folder, version != null ? version : "unknown"));
    }

    /** Returns the first regular file under {@code directory} matching {@code globPattern}, or null. */
    private static Path findMatch(Path directory, String globPattern) {
        if (directory == null || !Files.isDirectory(directory)) {
            return null;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, globPattern)) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry)) {
                    return entry;
                }
            }
        } catch (IOException e) {
            // Directory became unreadable mid-scan (permissions, race with external
            // deletion, etc.) — treat as "no match" rather than propagating.
        }
        return null;
    }

    private static String extractVersionFromFolderName(String folderName) {
        Matcher matcher = VERSION_PATTERN.matcher(folderName);
        return matcher.find() ? matcher.group(1) : null;
    }
}