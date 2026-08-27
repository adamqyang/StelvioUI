package com.github.adamqyang.install;

import java.util.List;

/**
 * Outcome of validating a candidate folder as a Stelvio installation.
 */
public sealed interface ValidationResult {

    /** The folder is a genuine Stelvio installation. */
    record Valid(StelvioInstallation installation) implements ValidationResult {
    }

    /** The folder is missing one or more expected files/subfolders. */
    record Invalid(List<String> missingItems) implements ValidationResult {
    }
}
