package com.github.adamqyang.output;

import java.util.Arrays;

/**
 * Dev-only: parses the real OutOfMemoryError sample shared during
 * development and confirms it's correctly recognized as a crash (not a
 * generic "unrecognized file" error), with the right Kind and an
 * actionable message.
 * <p>
 * Under src/test/java - never ships with the app.
 */
public class ProblemsOutParserCrashSmokeTest {

    private static final String OUT_OF_MEMORY_SAMPLE = """
             /* ****************************************************************************************** */
             /* Stelvio 4.5 Copyright 2023-26 Reto Aschwanden                                              */
             /* ****************************************************************************************** */


               Starting at: 19:43:58; 29.08.2026   Position [B4b2/r2ppp2/n1prRrRn/P1k4p/2NR2Q1/1bR2N1r/KBPPPP2/7q]

               **************************
               * B  .  .  .  .  b  .  . *
               * r  .  .  p  p  p  .  . *
               * s  .  p  r  R  r  R  s *
               * P  .  k  .  .  .  .  p *
               * .  .  S  R  .  .  Q  . *
               * .  b  R  .  .  S  .  r *
               * K  B  P  P  P  P  .  . *
               * .  .  .  .  .  .  .  q *
               **************************
               32.0              (15, 15)

            java.lang.OutOfMemoryError: Java heap space
            \tat ch.stelvio.b.b.a.a.c.<init>(Unknown Source)
            \tat ch.stelvio.b.b.a.a.j.<init>(Unknown Source)
            \tat ch.stelvio.a.c.b.d.<init>(Unknown Source)
            \tat ch.stelvio.ui.c.a(Unknown Source)
            \tat ch.stelvio.ui.a.a(Unknown Source)
            \tat ch.stelvio.ui.a.a(Unknown Source)
            \tat ch.stelvio.ui.StelvioUIMain.main(Unknown Source)
            """;

    public static void main(String[] args) {
        try {
            ProblemsOutParser.parse(Arrays.asList(OUT_OF_MEMORY_SAMPLE.split("\n")));
            System.out.println("FAIL: expected a StelvioCrashException, but parsing succeeded.");
        } catch (StelvioCrashException e) {
            System.out.println("Caught StelvioCrashException as expected.");
            System.out.println("Kind: " + e.kind());
            System.out.println("Message: " + e.getMessage());
            System.out.println(e.kind() == StelvioCrashException.Kind.OUT_OF_MEMORY
                    ? "PASS: correctly identified as OUT_OF_MEMORY."
                    : "FAIL: expected OUT_OF_MEMORY, got " + e.kind());
        } catch (Exception e) {
            System.out.println("FAIL: expected StelvioCrashException, got " + e.getClass().getSimpleName()
                    + ": " + e.getMessage());
        }
    }
}
