package com.github.adamqyang.install;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Scans a short list of plausible install locations for a valid Stelvio
 * installation. This is a targeted check of likely spots, not an exhaustive
 * filesystem search — keeps it fast and non-invasive.
 */
public final class InstallationLocator {

    private InstallationLocator() {
    }

    /**
     * Returns every valid Stelvio installation found among the candidate
     * locations. More than one result is possible if the user has multiple
     * versions extracted side by side (e.g. while testing an upgrade).
     */
    public static List<StelvioInstallation> scanCandidateLocations() {
        List<StelvioInstallation> found = new ArrayList<>();
        for (Path parent : candidateParentDirectories()) {
            found.addAll(findStelvioFoldersUnder(parent));
        }
        return found;
    }

    /** Validates a single, user-chosen folder (e.g. from a directory picker). */
    public static Optional<StelvioInstallation> validateChosenFolder(Path folder) {
        return asValid(InstallationValidator.validate(folder));
    }

    private static List<Path> candidateParentDirectories() {
        List<Path> candidates = new ArrayList<>();
        candidates.add(Paths.get("C:\\spg"));
        candidates.add(Paths.get("C:\\"));
        candidates.add(Paths.get("C:\\Program Files"));

        String userHome = System.getProperty("user.home");
        if (userHome != null) {
            candidates.add(Paths.get(userHome));
            candidates.add(Paths.get(userHome, "Desktop"));
            candidates.add(Paths.get(userHome, "Downloads"));
        }
        return candidates;
    }

    private static List<StelvioInstallation> findStelvioFoldersUnder(Path parent) {
        List<StelvioInstallation> results = new ArrayList<>();
        if (!Files.isDirectory(parent)) {
            return results;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(parent, "stelvio*")) {
            for (Path candidate : stream) {
                if (!Files.isDirectory(candidate)) {
                    continue;
                }
                asValid(InstallationValidator.validate(candidate)).ifPresent(results::add);
            }
        } catch (IOException e) {
            // Parent directory became unreadable mid-scan — skip it rather than propagate;
            // this is a convenience scan, not a required step.
        }
        return results;
    }

    private static Optional<StelvioInstallation> asValid(ValidationResult result) {
        if (result instanceof ValidationResult.Valid valid) {
            return Optional.of(valid.installation());
        }
        return Optional.empty();
    }
}
