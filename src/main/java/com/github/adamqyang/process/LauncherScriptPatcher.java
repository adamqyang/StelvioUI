package com.github.adamqyang.process;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.adamqyang.install.StelvioInstallation;

/**
 * Patches Stelvio's own launcher script (stelvio*.bat) in two ways, both
 * applied to a backed-up pristine original so every patch starts fresh:
 * <p>
 * 1. The -Xmx (max heap) value - a simple token substitution, since this
 *    part of the file format is stable and well-documented.
 * <p>
 * 2. The entire java-invocation line - replaced outright rather than
 *    surgically edited, since the java path a given Stelvio install was
 *    originally configured with varies too much across machines (quoting,
 *    spaces in "Program Files"-style paths, drive layout) to safely locate
 *    and edit in place. Instead we find that line by a stable marker (it's
 *    the one containing "-jar") and reconstruct it entirely from values we
 *    already trust: the JDK currently running THIS app (java.home -
 *    guaranteed correct, since it's literally what's executing this code),
 *    and the actual jar filename in this install's bin folder.
 * <p>
 * Backs up the original script (once per install, on first patch) to
 * "<scriptname>.original" before ever modifying it.
 */
public final class LauncherScriptPatcher {

    private static final Pattern XMX_PATTERN = Pattern.compile("-Xmx\\d+[a-zA-Z]");

    private LauncherScriptPatcher() {
    }

    public static void patch(StelvioInstallation installation, int ramGigabytes) throws IOException {
        Path batFile = installation.batFile();
        Path backupFile = batFile.resolveSibling(batFile.getFileName() + ".original");

        if (!Files.exists(backupFile)) {
            Files.copy(batFile, backupFile);
        }

        Path javawExe = resolveJavawExecutable();
        Path jarFile = installation.jarFile();

        List<String> originalLines = Files.readAllLines(backupFile);
        List<String> patchedLines = originalLines.stream()
                .map(line -> patchLine(line, ramGigabytes, javawExe, jarFile))
                .toList();

        Files.write(batFile, patchedLines);
    }

    private static String patchLine(String line, int ramGigabytes, Path javawExe, Path jarFile) {
        if (line.contains("-jar")) {
            return "\"" + javawExe + "\" %MEMORY% -jar \"bin\\" + jarFile.getFileName() + "\"";
        }
        Matcher matcher = XMX_PATTERN.matcher(line);
        if (matcher.find()) {
            return matcher.replaceAll("-Xmx" + ramGigabytes + "g");
        }
        return line;
    }

    /**
     * Locates javaw.exe within the JDK currently running this app
     * (java.home), verifying it actually exists rather than assuming so -
     * a broken or unusually trimmed JRE could theoretically be missing it.
     */
    private static Path resolveJavawExecutable() throws IOException {
        String javaHome = System.getProperty("java.home");
        if (javaHome == null || javaHome.isBlank()) {
            throw new IOException(
                    "Could not determine the Java installation running this app (java.home was empty). "
                            + "This shouldn't normally happen - please report this if you see it.");
        }
        Path javawExe = Path.of(javaHome, "bin", "javaw.exe");
        if (!Files.exists(javawExe)) {
            throw new IOException(
                    "Could not find javaw.exe in the Java installation running this app (looked in \""
                            + javawExe + "\"). Your Java installation may be incomplete or corrupted - "
                            + "try reinstalling a JDK and running this app again.");
        }
        return javawExe;
    }
}
