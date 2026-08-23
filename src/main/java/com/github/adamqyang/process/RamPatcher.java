package com.github.adamqyang.process;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import com.github.adamqyang.install.StelvioInstallation;

/**
 * Patches the -Xmx (max heap) value in Stelvio's own launcher script,
 * changing only that token and leaving everything else - java path,
 * classpath, main class, any other flags - exactly as the user already
 * configured it during Stelvio's own installation. Same "patch, don't
 * rewrite" philosophy as StelvioIniPatcher, and for the same reasons: this
 * avoids having to guess at java path detection or classpath construction
 * ourselves, and keeps the file maximally close to what the user already
 * has working.
 * <p>
 * Backs up the original script (once per install, on first patch) to
 * "<scriptname>.original" before ever modifying it, mirroring
 * StelvioIniPatcher's backup approach.
 */
public final class RamPatcher {

    private static final Pattern XMX_PATTERN = Pattern.compile("-Xmx\\d+[a-zA-Z]");

    private RamPatcher() {
    }

    public static void patch(StelvioInstallation installation, int ramGigabytes) throws IOException {
        Path batFile = installation.batFile();
        Path backupFile = batFile.resolveSibling(batFile.getFileName() + ".original");

        if (!Files.exists(backupFile)) {
            Files.copy(batFile, backupFile);
        }

        String original = Files.readString(backupFile);
        String patched = XMX_PATTERN.matcher(original).replaceAll("-Xmx" + ramGigabytes + "g");
        Files.writeString(batFile, patched);
    }
}
