package com.github.adamqyang.install;

/**
 * Dev-only utility to clear the remembered Stelvio installation, so the
 * install-detection screen can be re-tested from a clean state.
 */
public class ClearSavedInstallation {
    public static void main(String[] args) {
        new InstallationPreferences().forget();
        System.out.println("Cleared remembered Stelvio installation.");
    }
}