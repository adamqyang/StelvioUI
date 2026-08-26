package com.github.adamqyang.ui;

import java.io.IOException;

import com.github.adamqyang.chess.FenValidator;
import com.github.adamqyang.chess.Position;
import com.github.adamqyang.config.IniParameterDescriptions;
import com.github.adamqyang.config.MoveCount;
import com.github.adamqyang.config.ProblemsFileWriter;
import com.github.adamqyang.config.SolveRequest;
import com.github.adamqyang.config.StelvioIniPatcher;
import com.github.adamqyang.config.StelvioSettings;
import com.github.adamqyang.install.StelvioInstallation;
import com.github.adamqyang.process.LauncherScriptPatcher;
import com.github.adamqyang.process.StelvioLauncher;
import com.github.adamqyang.process.WindowsStelvioLauncher;
import com.github.adamqyang.ui.component.BoardView;

import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

public class InputScreenController {

    @FXML private Label installationLabel;
    @FXML private Button changeFolderButton;
    @FXML private TextField fenField;
    @FXML private BoardView fenPreviewBoard;
    @FXML private TextField moveCountField;
    @FXML private TextField strategyConditionsField;
    @FXML private Spinner<Integer> ramGigabytesSpinner;
    @FXML private CheckBox histogramModeCheckBox;
    @FXML private CheckBox pgnOutputCheckBox;
    @FXML private ComboBox<StelvioSettings.RetractionMode> retractionModeComboBox;
    @FXML private ComboBox<StelvioSettings.CollisionDetectionMode> collisionModeComboBox;
    @FXML private Spinner<Integer> numSeekersSpinner;
    @FXML private Spinner<Integer> numPlayersSpinner;
    @FXML private Spinner<Integer> stopAfterXCooksSpinner;
    @FXML private Spinner<Integer> maxSolutionsPerCookSpinner;
    @FXML private Button moreSettingsButton;
    @FXML private Button solveButton;
    @FXML private Label errorLabel;
    @FXML private Label statusLabel;

    private StelvioInstallation installation;

    @FXML
    public void initialize() {
        fenField.textProperty().addListener((obs, oldValue, newValue) -> updateFenPreview(newValue));

        StelvioSettings defaults = new StelvioSettings();

        // 8 GB matches Stelvio's own documented default -Xmx in a fresh install's .bat file.
        ramGigabytesSpinner.setValueFactory(intSpinnerFactory(1, 128, 8));
        numSeekersSpinner.setValueFactory(intSpinnerFactory(1, 64, defaults.getNumStrategySeekers()));
        numPlayersSpinner.setValueFactory(intSpinnerFactory(1, 64, defaults.getNumStrategyPlayers()));
        stopAfterXCooksSpinner.setValueFactory(intSpinnerFactory(1, 1000, defaults.getStopAfterXCooks()));
        maxSolutionsPerCookSpinner.setValueFactory(intSpinnerFactory(1, 100, defaults.getMaxSolutionsPerCook()));
        makeEditableAndCommitOnFocusLoss(ramGigabytesSpinner);
        makeEditableAndCommitOnFocusLoss(numSeekersSpinner);
        makeEditableAndCommitOnFocusLoss(numPlayersSpinner);
        makeEditableAndCommitOnFocusLoss(stopAfterXCooksSpinner);
        makeEditableAndCommitOnFocusLoss(maxSolutionsPerCookSpinner);

        retractionModeComboBox.getItems().setAll(StelvioSettings.RetractionMode.values());
        retractionModeComboBox.setValue(defaults.getRetractionMode());
        retractionModeComboBox.setConverter(retractionModeConverter());

        collisionModeComboBox.getItems().setAll(StelvioSettings.CollisionDetectionMode.values());
        collisionModeComboBox.setValue(defaults.getExpensiveCollisionDetectionMode());
        collisionModeComboBox.setConverter(collisionModeConverter());

        histogramModeCheckBox.setSelected(defaults.isHistogramMode());
        pgnOutputCheckBox.setSelected(defaults.isPgnOutput());

        // Tooltips sourced from the same descriptions the ini comments use -
        // one source of truth, no separate help text to maintain here.
        Tooltip.install(histogramModeCheckBox, tooltipFor("histogramMode"));
        Tooltip.install(pgnOutputCheckBox, tooltipFor("pgnOutput"));
        Tooltip.install(retractionModeComboBox, tooltipFor("retractionMode"));
        Tooltip.install(collisionModeComboBox, tooltipFor("expensiveCollisionDetectionMode"));
        Tooltip.install(numSeekersSpinner, tooltipFor("numStrategySeekers"));
        Tooltip.install(numPlayersSpinner, tooltipFor("numStrategyPlayers"));
        Tooltip.install(stopAfterXCooksSpinner, tooltipFor("stopAfterXCooks"));
        Tooltip.install(maxSolutionsPerCookSpinner, tooltipFor("maxSolutionsPerCook"));
        // Not an ini key (it's a JVM launch flag in the .bat file), so this text
        // lives here rather than in IniParameterDescriptions.
        Tooltip.install(ramGigabytesSpinner, new Tooltip(
                "Maximum memory Stelvio is allowed to use. More can help with complex positions, but "
                        + "giving it far more than a problem actually needs can slow things down due to "
                        + "cache initialization overhead."));
    }

    /** Called by InstallScreenController (or MainApp, on an auto-skipped startup) right after navigating here. */
    public void setInstallation(StelvioInstallation installation) {
        this.installation = installation;
        installationLabel.setText("Using Stelvio " + installation.version() + " \u2014 " + installation.folder());
    }

    /**
     * Updates the board preview as the user types. Stays quiet (leaves the
     * board showing whatever it last had) while the FEN is mid-typing and
     * therefore invalid - re-validating and erroring on every keystroke
     * would be distracting rather than helpful.
     */
    private void updateFenPreview(String fenText) {
        if (fenText == null || fenText.isBlank()) {
            fenPreviewBoard.clear();
            return;
        }
        try {
            fenPreviewBoard.show(Position.fromFen(fenText));
        } catch (IllegalArgumentException e) {
            // Incomplete/invalid mid-typing - leave the board as it was.
        }
    }

    @FXML
    private void onChangeFolderClicked() {
        // Deliberately navigating back to the install screen here - unlike the
        // auto-skip MainApp does at startup, this always shows its normal
        // confirmed/searching UI, giving the user a real choice (keep, rescan,
        // or browse) rather than bouncing straight back to this screen.
        SceneNavigator.showView("/fxml/install-screen.fxml");
    }

    @FXML
    private void onSolveClicked() {
        errorLabel.setText("");
        statusLabel.setText("");

        if (installation == null) {
            errorLabel.setText("No Stelvio installation selected. Go back and choose one first.");
            return;
        }
        try {
            FenValidator.validate(fenField.getText());
        } catch (IllegalArgumentException e) {
            errorLabel.setText(e.getMessage());
            return;
        }

        MoveCount moveCount;
        try {
            moveCount = MoveCount.parse(moveCountField.getText());
        } catch (IllegalArgumentException e) {
            errorLabel.setText(e.getMessage());
            return;
        }

        StelvioSettings settings = new StelvioSettings();
        settings.setHistogramMode(histogramModeCheckBox.isSelected());
        settings.setPgnOutput(pgnOutputCheckBox.isSelected());
        settings.setRetractionMode(retractionModeComboBox.getValue());
        settings.setExpensiveCollisionDetectionMode(collisionModeComboBox.getValue());
        settings.setNumStrategySeekers(numSeekersSpinner.getValue());
        settings.setNumStrategyPlayers(numPlayersSpinner.getValue());
        settings.setStopAfterXCooks(stopAfterXCooksSpinner.getValue());
        settings.setMaxSolutionsPerCook(maxSolutionsPerCookSpinner.getValue());

        SolveRequest request = new SolveRequest(
                fenField.getText().trim(),
                moveCount,
                strategyConditionsField.getText(),
                settings
        );

        try {
            ProblemsFileWriter.write(installation.folder(), request);
            StelvioIniPatcher.patch(installation.folder(), request.settings());
            LauncherScriptPatcher.patch(installation, ramGigabytesSpinner.getValue());
        } catch (IOException e) {
            errorLabel.setText("Failed to prepare Stelvio's files: " + e.getMessage());
            return;
        }

        beginLaunchSequence();
    }

    /**
     * Shows a brief status message, then minimizes our window and launches
     * Stelvio - restoring and refocusing our window once Stelvio's window is
     * closed. The short pause before minimizing (rather than minimizing
     * immediately) exists purely so the status message actually gets a chance
     * to render before the window disappears.
     */
    private void beginLaunchSequence() {
        solveButton.setDisable(true);
        statusLabel.setText("Launching Stelvio \u2014 this window will minimize and reappear "
                + "automatically when Stelvio closes.");

        Stage stage = (Stage) solveButton.getScene().getWindow();

        PauseTransition delay = new PauseTransition(Duration.millis(900));
        delay.setOnFinished(event -> {
            stage.setIconified(true);
            launchStelvio(stage);
        });
        delay.play();
    }

    private void launchStelvio(Stage stage) {
        StelvioLauncher launcher = new WindowsStelvioLauncher();
        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() throws Exception {
                return launcher.launchAndWait(installation);
            }
        };
        task.setOnSucceeded(e -> {
            solveButton.setDisable(false);
            stage.setIconified(false);
            stage.toFront();
            stage.requestFocus();
            int exitCode = task.getValue();
            statusLabel.setText("Stelvio finished (exit code " + exitCode + "). "
                    + "(Reading problems_out.txt comes next.)");
        });
        task.setOnFailed(e -> {
            solveButton.setDisable(false);
            stage.setIconified(false);
            stage.toFront();
            stage.requestFocus();
            Throwable ex = task.getException();
            errorLabel.setText("Failed to launch Stelvio: " + (ex != null ? ex.getMessage() : "unknown error"));
        });
        Thread thread = new Thread(task, "stelvio-launch");
        thread.setDaemon(true);
        thread.start();
    }

    private static SpinnerValueFactory<Integer> intSpinnerFactory(int min, int max, int initial) {
        return new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, initial);
    }

    /**
     * Spinners don't commit typed text until the editor loses focus or Enter
     * is pressed - clicking elsewhere without either silently discards the
     * typed value, so we force a commit on focus loss.
     */
    private static void makeEditableAndCommitOnFocusLoss(Spinner<Integer> spinner) {
        spinner.setEditable(true);
        spinner.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                spinner.increment(0); // commits the editor's text into the value
            }
        });
    }

    private static Tooltip tooltipFor(String iniKey) {
        return new Tooltip(IniParameterDescriptions.describe(iniKey));
    }

    private static StringConverter<StelvioSettings.RetractionMode> retractionModeConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(StelvioSettings.RetractionMode mode) {
                if (mode == null) {
                    return "";
                }
                return switch (mode) {
                    case NONE -> "None";
                    case KING_IN_CHECK -> "King in check (recommended)";
                };
            }

            @Override
            public StelvioSettings.RetractionMode fromString(String string) {
                throw new UnsupportedOperationException("Not editable");
            }
        };
    }

    private static StringConverter<StelvioSettings.CollisionDetectionMode> collisionModeConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(StelvioSettings.CollisionDetectionMode mode) {
                if (mode == null) {
                    return "";
                }
                return switch (mode) {
                    case DEFAULT -> "Default (auto)";
                    case ON -> "On (thorough, slower)";
                    case OFF -> "Off (fast, less thorough)";
                };
            }

            @Override
            public StelvioSettings.CollisionDetectionMode fromString(String string) {
                throw new UnsupportedOperationException("Not editable");
            }
        };
    }
}