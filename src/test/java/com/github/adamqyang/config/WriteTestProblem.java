package com.github.adamqyang.config;

import java.nio.file.Path;

/**
 * Dev-only: writes problems.txt and patches stelvioUI.ini in a real Stelvio
 * install folder, using the FEN/half-move example straight from Stelvio's
 * own documentation ("Input / Output" section). After running this, launch
 * Stelvio's own stelvio*.bat by hand and confirm it accepts and solves what
 * was written - validating the full round-trip against the real program.
 * <p>
 * Under src/test/java - never ships with the app.
 */
public class WriteTestProblem {

    public static void main(String[] args) throws Exception {
        // TODO: point this at your real Stelvio folder before running.
        // IMPORTANT: this folder's stelvioUI.ini must currently be the genuine,
        // untouched original - that's what gets backed up to stelvioUI.ini.original
        // on this first run, and every future patch is applied against that backup.
        Path installFolder = Path.of("C:\\Users\\Lenovo\\Desktop\\stelvio4.5");

        SolveRequest request = new SolveRequest(
                "1nbq4/ppk1p3/Rp5p/3npr2/R3P3/2br1B1P/PP2P2P/1NBQNK2",
                MoveCount.parse("32.5"),
                null,
                new StelvioSettings()
        );

        ProblemsFileWriter.write(installFolder, request);
        StelvioIniPatcher.patch(installFolder, request.settings());

        System.out.println("Wrote problems.txt and patched stelvioUI.ini in " + installFolder);
        System.out.println("Now run Stelvio's own .bat file in that folder to confirm it solves it.");
    }
}