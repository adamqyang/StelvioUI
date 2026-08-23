package com.github.adamqyang.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Applies a StelvioSettings' curated values on top of the Stelvio install's
 * OWN stelvioUI.ini, changing only the value portion of matching
 * "key = value" lines. Every other line - comments, blank lines, formatting,
 * whitespace, and any key we don't curate - is left completely untouched.
 * <p>
 * Deliberately a patch, not a from-scratch rewrite: it keeps this wrapper's
 * footprint on Stelvio's own files as small as possible, avoids any risk of
 * us guessing wrong about a formatting/parsing detail Stelvio's own shipped
 * file already gets right, and means anyone who opens the file by hand still
 * sees the same comments and layout the original author wrote.
 * <p>
 * The first time this runs against a given install folder, the current
 * stelvioUI.ini is backed up to stelvioUI.ini.original (only if that backup
 * doesn't already exist) - so every future patch is always applied against a
 * pristine copy, never against output from a previous run of this tool.
 */
public final class StelvioIniPatcher {

    private static final String INI_FILE_NAME = "stelvioUI.ini";
    private static final String BACKUP_FILE_NAME = "stelvioUI.ini.original";
    private static final String INPUT_FILE_NAME = "problems.txt";
    private static final String OUTPUT_FILE_NAME = "problems_out.txt";

    // Matches lines like "  key = value" or "key=value", capturing the leading
    // whitespace, the key, and the "=" with its surrounding spacing exactly as
    // written - so patched lines keep the original's formatting except for
    // the value itself.
    private static final Pattern ASSIGNMENT_LINE =
            Pattern.compile("^(\\s*)([A-Za-z][A-Za-z0-9]*)(\\s*=\\s*)(.*)$");

    private StelvioIniPatcher() {
    }

    public static void patch(Path installFolder, StelvioSettings settings) throws IOException {
        Path iniPath = installFolder.resolve(INI_FILE_NAME);
        Path backupPath = installFolder.resolve(BACKUP_FILE_NAME);

        if (!Files.exists(backupPath)) {
            Files.copy(iniPath, backupPath);
        }

        Map<String, String> overrides = settings.toIniProperties();
        // Always forced regardless of settings, so our own file writer/output
        // parser can never disagree with Stelvio about where its files live.
        overrides.put("inputFileName", INPUT_FILE_NAME);
        overrides.put("outputFileName", OUTPUT_FILE_NAME);

        List<String> originalLines = Files.readAllLines(backupPath);
        List<String> patchedLines = originalLines.stream()
                .map(line -> patchLine(line, overrides))
                .toList();

        // Uses the platform line separator (CRLF on Windows), matching how the
        // original Windows-authored file is already formatted.
        Files.write(iniPath, patchedLines);
    }

    private static String patchLine(String line, Map<String, String> overrides) {
        Matcher matcher = ASSIGNMENT_LINE.matcher(line);
        if (!matcher.matches()) {
            return line; // comment, blank line, or anything else - leave untouched
        }
        String key = matcher.group(2);
        String newValue = overrides.get(key);
        if (newValue == null) {
            return line; // a key we don't curate - leave its original value untouched
        }
        return matcher.group(1) + key + matcher.group(3) + newValue;
    }
}
