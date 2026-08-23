package com.github.adamqyang.install;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Represents a confirmed, validated Stelvio installation folder.
 *
 * @param folder  the root Stelvio installation directory (e.g. "C:\spg\stelvio4.5")
 * @param version the detected Stelvio version string (e.g. "4.5"), or "unknown"
 *                if it could not be parsed from the launcher script's filename
 */
public record StelvioInstallation(Path folder, String version) {

    /**
     * Locates this installation's stelvio*.bat launcher script. Since
     * InstallationValidator already confirmed one exists when this record
     * was created, this should always succeed unless the file was moved or
     * deleted afterward.
     */
    public Path batFile() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder, "stelvio*.bat")) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry)) {
                    return entry;
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not search " + folder + " for stelvio*.bat", e);
        }
        throw new IllegalStateException(
                "No stelvio*.bat found in " + folder + " - was it moved or deleted since being selected?");
    }
}