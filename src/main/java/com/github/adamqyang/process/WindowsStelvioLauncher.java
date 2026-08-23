package com.github.adamqyang.process;

import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Pattern;

import com.github.adamqyang.install.StelvioInstallation;

/**
 * Runs Stelvio's own stelvio*.bat via cmd.exe. Deliberately does NOT wrap
 * this in "cmd /c start ..." - cmd.exe's built-in "start" command re-parses
 * its argument as its own command line rather than treating it as a clean
 * list of arguments, which would reopen exactly the shell-metacharacter risk
 * we want to avoid for a user-chosen install folder path.
 * <p>
 * A GUI process (like this one) has no attached console, so spawning
 * cmd.exe /c directly already causes Windows to allocate a brand-new,
 * visible console window for it automatically - no "start" trick needed to
 * get that behavior. Stelvio's own terminal UI then takes over that window
 * exactly as it would if launched by hand.
 */
public final class WindowsStelvioLauncher implements StelvioLauncher {

    // cmd.exe treats these as special even inside what Java considers a single,
    // already-quoted argument, since cmd.exe re-parses its /c argument as its
    // own command line. A folder path containing any of these could otherwise
    // let its name be interpreted as extra commands rather than a plain path.
    private static final Pattern UNSAFE_CMD_CHARACTERS = Pattern.compile("[&|^%<>()!\"]");

    @Override
    public int launchAndWait(StelvioInstallation installation) throws IOException, InterruptedException {
        Path batFile = installation.batFile();
        requireSafeForCmd(batFile);

        ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", batFile.toString());
        builder.directory(installation.folder().toFile());
        // Not redirected: Stelvio's own terminal UI needs its own console to render into.
        Process process = builder.start();
        return process.waitFor();
    }

    private static void requireSafeForCmd(Path batFile) throws IOException {
        if (UNSAFE_CMD_CHARACTERS.matcher(batFile.toString()).find()) {
            throw new IOException(
                    "Your Stelvio folder path contains characters that can't be safely used to launch "
                            + "the program (one of & | ^ % < > ( ) ! \"). Please move the Stelvio folder "
                            + "to a path without these characters and select it again.");
        }
    }
}
