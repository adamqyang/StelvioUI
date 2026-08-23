package com.github.adamqyang.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The stelvioUI.ini parameters we expose up-front in the UI. Deliberately a
 * curated subset, not the full ~35 documented parameters - StelvioIniPatcher
 * applies these on top of the install's own, already-valid stelvioUI.ini,
 * so anything we don't curate here simply keeps whatever value that file
 * already had.
 * <p>
 * Field names intentionally match Stelvio's ini keys exactly (StelvioParameters
 * loads the file via reflection on these names), so {@link #toIniProperties()}
 * can just be a direct name-to-value mapping.
 */
public final class StelvioSettings {

    public enum CollisionDetectionMode {
        DEFAULT("default"), ON("on"), OFF("off");

        private final String iniValue;

        CollisionDetectionMode(String iniValue) {
            this.iniValue = iniValue;
        }

        public String iniValue() {
            return iniValue;
        }
    }

    public enum RetractionMode {
        NONE("none"), KING_IN_CHECK("kingInCheck");

        private final String iniValue;

        RetractionMode(String iniValue) {
            this.iniValue = iniValue;
        }

        public String iniValue() {
            return iniValue;
        }
    }

    // Parallelizing — the two knobs the docs most directly tie to your hardware.
    private int numStrategySeekers = 2;
    private int numStrategyPlayers = 1;

    // Strategy analysis
    private CollisionDetectionMode expensiveCollisionDetectionMode = CollisionDetectionMode.DEFAULT;

    // Strategy playing
    private RetractionMode retractionMode = RetractionMode.KING_IN_CHECK;

    // General
    private boolean histogramMode = false;

    // Input/Output — defaults to true here (Stelvio itself defaults to false),
    // since it's directly useful for the copyable-PGN feature planned for the
    // results screen.
    private boolean pgnOutput = true;

    // Cook parameters
    private int stopAfterXCooks = 1;
    private int maxSolutionsPerCook = 2;

    public int getNumStrategySeekers() {
        return numStrategySeekers;
    }

    public void setNumStrategySeekers(int numStrategySeekers) {
        this.numStrategySeekers = numStrategySeekers;
    }

    public int getNumStrategyPlayers() {
        return numStrategyPlayers;
    }

    public void setNumStrategyPlayers(int numStrategyPlayers) {
        this.numStrategyPlayers = numStrategyPlayers;
    }

    public CollisionDetectionMode getExpensiveCollisionDetectionMode() {
        return expensiveCollisionDetectionMode;
    }

    public void setExpensiveCollisionDetectionMode(CollisionDetectionMode mode) {
        this.expensiveCollisionDetectionMode = mode;
    }

    public RetractionMode getRetractionMode() {
        return retractionMode;
    }

    public void setRetractionMode(RetractionMode retractionMode) {
        this.retractionMode = retractionMode;
    }

    public boolean isHistogramMode() {
        return histogramMode;
    }

    public void setHistogramMode(boolean histogramMode) {
        this.histogramMode = histogramMode;
    }

    public boolean isPgnOutput() {
        return pgnOutput;
    }

    public void setPgnOutput(boolean pgnOutput) {
        this.pgnOutput = pgnOutput;
    }

    public int getStopAfterXCooks() {
        return stopAfterXCooks;
    }

    public void setStopAfterXCooks(int stopAfterXCooks) {
        this.stopAfterXCooks = stopAfterXCooks;
    }

    public int getMaxSolutionsPerCook() {
        return maxSolutionsPerCook;
    }

    public void setMaxSolutionsPerCook(int maxSolutionsPerCook) {
        this.maxSolutionsPerCook = maxSolutionsPerCook;
    }

    /**
     * Returns the ini key/value pairs for these curated settings. Does NOT
     * include inputFileName/outputFileName - StelvioIniPatcher forces those
     * itself regardless of what's in here.
     */
    public Map<String, String> toIniProperties() {
        Map<String, String> props = new LinkedHashMap<>();
        props.put("histogramMode", String.valueOf(histogramMode));
        props.put("pgnOutput", String.valueOf(pgnOutput));
        props.put("retractionMode", retractionMode.iniValue());
        props.put("numStrategySeekers", String.valueOf(numStrategySeekers));
        props.put("numStrategyPlayers", String.valueOf(numStrategyPlayers));
        props.put("expensiveCollisionDetectionMode", expensiveCollisionDetectionMode.iniValue());
        props.put("stopAfterXCooks", String.valueOf(stopAfterXCooks));
        props.put("maxSolutionsPerCook", String.valueOf(maxSolutionsPerCook));
        return props;
    }
}