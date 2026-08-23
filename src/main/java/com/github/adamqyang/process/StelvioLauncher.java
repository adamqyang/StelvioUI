package com.github.adamqyang.process;

import java.io.IOException;

import com.github.adamqyang.install.StelvioInstallation;

/**
 * Launches Stelvio's own launcher script and blocks until it exits.
 * Windows-only implementation for now (matches the project's current
 * scope); this interface boundary keeps a future Linux/Mac implementation
 * contained to its own class rather than requiring changes elsewhere.
 */
public interface StelvioLauncher {

    /**
     * Launches Stelvio and blocks until the process exits.
     *
     * @return the process's exit code
     */
    int launchAndWait(StelvioInstallation installation) throws IOException, InterruptedException;
}
