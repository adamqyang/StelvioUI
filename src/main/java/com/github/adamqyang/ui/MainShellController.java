package com.github.adamqyang.ui;

import com.github.adamqyang.install.StelvioInstallation;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * The persistent shell shown once a Stelvio installation is confirmed -
 * everything downstream (input, results, and future screens) lives inside
 * this as a tab, rather than each screen fully replacing the window the
 * way the install-screen gate does.
 * <p>
 * input-screen.fxml and results-screen.fxml are loaded here via
 * SceneNavigator.load() and embedded as tab content, rather than being
 * declared inline in this FXML - both screens remain complete, standalone
 * units, unaware of and unaffected by being shown inside a tab versus as a
 * whole window.
 */
public class MainShellController {

    @FXML private TabPane tabPane;
    @FXML private Tab inputTab;
    @FXML private Tab resultsTab;

    private InputScreenController inputController;
    private ResultsScreenController resultsController;

    @FXML
    public void initialize() {
        SceneNavigator.LoadedView<InputScreenController> inputView =
                SceneNavigator.load("/fxml/input-screen.fxml");
        inputTab.setContent(inputView.root());
        inputController = inputView.controller();

        SceneNavigator.LoadedView<ResultsScreenController> resultsView =
                SceneNavigator.load("/fxml/results-screen.fxml");
        resultsTab.setContent(resultsView.root());
        resultsController = resultsView.controller();

        // Task's succeeded handler (where this callback ultimately fires from)
        // already runs on the JavaFX Application Thread, so it's safe to touch
        // the UI directly here - no Platform.runLater needed.
        inputController.setOnSolveComplete((result, outputFile) -> {
            resultsController.showResult(result, outputFile);
            showResultsTab();
        });
    }

    /** Called right after the shell is shown, to pass along the confirmed installation. */
    public void setInstallation(StelvioInstallation installation) {
        inputController.setInstallation(installation);
    }

    public void showResultsTab() {
        tabPane.getSelectionModel().select(resultsTab);
    }

    public ResultsScreenController resultsController() {
        return resultsController;
    }
}