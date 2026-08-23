package com.github.adamqyang.install;

import java.util.List;

/**
 * Outcome of validating a candidate folder as a Stelvio installation.
 * <p>
 * Using a sealed interface here (rather than, say, a boolean + nullable
 * fields) means the compiler can check that every place we handle a
 * ValidationResult accounts for both possibilities.
 */
public sealed interface ValidationResult {

    /** The folder is a genuine Stelvio installation. */
    record Valid(StelvioInstallation installation) implements ValidationResult {
    }

    /** The folder is missing one or more expected files/subfolders. */
    record Invalid(List<String> missingItems) implements ValidationResult {
    }
}
