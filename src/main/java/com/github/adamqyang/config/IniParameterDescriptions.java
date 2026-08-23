package com.github.adamqyang.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Human-readable descriptions for each documented stelvioUI.ini parameter,
 * paraphrased from Stelvio's own documentation (StelvioParameters.pdf). This
 * is the single source of truth for parameter help text - StelvioIniWriter
 * uses it to write comments into the generated ini file, and the eventual
 * settings screen will reuse the same text for tooltips/help labels, so the
 * two can never drift out of sync with each other.
 */
public final class IniParameterDescriptions {

    private static final Map<String, String> DESCRIPTIONS = buildDescriptions();

    private IniParameterDescriptions() {
    }

    /** Returns the description for the given ini key, or an empty string if none is known. */
    public static String describe(String iniKey) {
        return DESCRIPTIONS.getOrDefault(iniKey, "");
    }

    private static Map<String, String> buildDescriptions() {
        Map<String, String> d = new LinkedHashMap<>();

        d.put("histogramMode", "Instead of playing strategies, only counts them and shows a "
                + "histogram grouped by white/black free moves - useful for a quick sense of "
                + "whether a problem is solvable in reasonable time.");
        d.put("includeTestWithHalfMoveLess", "If enabled, every problem is solved a second time "
                + "with one fewer half-move, guaranteeing any shorter solution is also found.");

        d.put("pgnOutput", "Writes solutions/cooks in PGN format. Move notation is always fully "
                + "qualified either way, so this mainly affects formatting for tools like lichess.org.");

        d.put("saveEveryXStrategiesToFile", "Saves found strategies to disk in batches of this "
                + "size, so they can be replayed later without recalculating them. -1 disables saving.");
        d.put("readStrategiesFromFile", "Reads previously saved strategies from disk instead of "
                + "searching for them again.");

        d.put("retractionMode", "Controls how a king-in-check diagram position is handled. "
                + "\"kingInCheck\" retracts the last half-move and solves the resulting shorter "
                + "position(s); \"none\" disables this.");
        d.put("whitePathSplitPieces", "Splits strategies with white free moves into sub-strategies "
                + "by distributing those free moves in every possible way. \"none\" disables this; "
                + "\"all2\", \"all4\", ... \"all100\" distribute up to that many free moves.");
        d.put("blackPathSplitPieces", "Same as whitePathSplitPieces, but for black's free moves.");
        d.put("simulatePlaying", "Only meaningful with whitePathSplitPieces/blackPathSplitPieces "
                + "set. Calculates how many sub-strategies splitting would produce, without actually "
                + "playing them.");
        d.put("minWhiteFreeMovesInclusive", "Only plays strategies with at least this many white "
                + "free moves. -1 means no restriction.");
        d.put("minBlackFreeMovesInclusive", "Only plays strategies with at least this many black "
                + "free moves. -1 means no restriction.");
        d.put("minTotalFreeMovesInclusive", "Only plays strategies with at least this many total "
                + "free moves. -1 means no restriction.");
        d.put("maxWhiteFreeMovesInclusive", "Only plays strategies with at most this many white "
                + "free moves. -1 means no restriction.");
        d.put("maxBlackFreeMovesInclusive", "Only plays strategies with at most this many black "
                + "free moves. -1 means no restriction.");
        d.put("maxTotalFreeMovesInclusive", "Only plays strategies with at most this many total "
                + "free moves. -1 means no restriction.");

        d.put("numStrategySeekers", "Number of parallel threads searching for strategies. Higher "
                + "values help most when a problem has many captures or promotions.");
        d.put("numStrategyPlayers", "Number of parallel threads playing (testing) strategies. Each "
                + "player needs its own position cache, so raising this can backfire on memory-hungry "
                + "problems.");
        d.put("strategyQueueSize", "Maximum number of strategies the seekers can queue up for the "
                + "players. Seekers wait once this fills.");
        d.put("strategySeekingSyncDepth", "Recursion depth at which parallel strategy seekers "
                + "synchronize. Lower values mean bigger work chunks; higher values (up to ~18) can "
                + "help with very skewed search trees.");
        d.put("numSlavePlayersPerStrategy", "Number of threads used to play a single strategy in "
                + "parallel. These share one position cache, so this is generally safe to raise.");
        d.put("slavePlayerHalfMoveSyncDepth", "Recursion depth, in half-moves, at which those slave "
                + "players synchronize. Similar tradeoff to strategySeekingSyncDepth.");

        d.put("printStrategies", "Writes every strategy into the output file. Can produce huge "
                + "output files, since strategies can number in the tens of millions.");
        d.put("collectSeekPartMetricsAfterXCycles", "How often strategy-seeking progress info is "
                + "gathered for display. -1 disables it entirely (fastest); 1 updates most often "
                + "(small performance cost).");

        d.put("startAtStrategyNr", "Skips the first N strategies. Only reliable with a single "
                + "strategy seeker, since strategy numbers aren't well-defined in parallel mode.");

        d.put("expensiveCollisionDetectionMode", "Controls how thoroughly Stelvio checks for piece "
                + "collisions. \"on\" is slower but catches more; \"off\" is faster but catches less; "
                + "\"default\" picks automatically based on piece count.");
        d.put("advancedCycleDetectionMode", "Same on/off/default tradeoff as "
                + "expensiveCollisionDetectionMode, but for cycle detection.");
        d.put("speculativeSplitterMode", "Controls whether ambiguous two-move paths are split into "
                + "sub-strategies to potentially rule out impossible options earlier. See the full "
                + "documentation for the complete none/probe/always/alwaysN option set.");

        d.put("maxSolutionsPerCook", "Number of example cook-solutions added to the output file per "
                + "cook-strategy found.");
        d.put("stopAfterXCooks", "Stops solving once this many cook strategies have been found.");
        d.put("printCookStrategy", "Additionally writes the strategy itself to the output file for "
                + "each found cook.");

        d.put("positionCacheMaxOverallExponent", "Caps the main position cache size (as 2^N "
                + "positions), overriding the automatic RAM-based estimate. Values below 25 are "
                + "ignored; -1 uses the automatic estimate.");
        d.put("positionCacheSplitExponent", "How many parts the cache array is split into. Affects "
                + "performance in ways not yet fully characterized by Stelvio's own author.");
        d.put("positionCacheMaxChainLength", "How long to search for a free/matching slot in the "
                + "cache. Should probably be greater than 100.");
        d.put("positionCacheFullEvictionThreshold", "Cache fill fraction, between 0 and 1, that "
                + "triggers eviction. A value above 1 disables eviction entirely.");
        d.put("positionCacheSizeAdjustment", "Adjusts the secondary cache size: -1 shrinks it "
                + "(freeing memory for other needs), 0 leaves it alone, 1 maximizes it (risking an "
                + "out-of-memory error).");

        return d;
    }
}
