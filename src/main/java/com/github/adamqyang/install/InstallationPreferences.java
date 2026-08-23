package com.github.adamqyang.install;

import java.nio.file.Paths;
import java.util.Optional;
import java.util.prefs.Preferences;

/**
 * Remembers the last confirmed Stelvio installation across app restarts,
 * using the per-user Preferences store (backed by the registry on Windows).
 * No config file for us to manage or clean up.
 */
public final class InstallationPreferences {

    private static final String KEY_FOLDER = "stelvioFolder";

    private final Preferences preferences;

    public InstallationPreferences() {
        this.preferences = Preferences.userNodeForPackage(InstallationPreferences.class);
    }

    public void remember(StelvioInstallation installation) {
        preferences.put(KEY_FOLDER, installation.folder().toString());
    }

    public void forget() {
        preferences.remove(KEY_FOLDER);
    }

    /**
     * Returns the previously remembered installation, but only if it still
     * passes validation today — the folder could have been moved, deleted,
     * or replaced with a different version since it was last confirmed.
     */
    public Optional<StelvioInstallation> loadIfStillValid() {
        String savedPath = preferences.get(KEY_FOLDER, null);
        if (savedPath == null) {
            return Optional.empty();
        }
        return InstallationLocator.validateChosenFolder(Paths.get(savedPath));
    }
}
