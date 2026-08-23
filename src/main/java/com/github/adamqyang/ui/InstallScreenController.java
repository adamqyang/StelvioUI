package com.github.adamqyang.ui;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.github.adamqyang.install.InstallationLocator;
import com.github.adamqyang.install.InstallationPreferences;
import com.github.adamqyang.install.StelvioInstallation;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.DirectoryChooser;

public class InstallScreenController {

    @FXML private Label statusLabel;
    @FXML private ListView<StelvioInstallation> foundInstallationsListView;
    @FXML private Label selectedPathLabel;
    @FXML private Label errorLabel;
    @FXML private Button browseButton;
    @FXML private Button continueButton;

    private final InstallationPreferences preferences = new InstallationPreferences();
    private StelvioInstallation selectedInstallation;

    // Called automatically by FXMLLoader after the FXML fields above are injected.
    @FXML
    public void initialize() {
        foundInstallationsListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(StelvioInstallation item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null
                        ? null
                        : "Stelvio " + item.version() + " \u2014 " + item.folder());
            }
        });
        foundInstallationsListView.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> {
                    if (newValue != null) {
                        selectInstallation(newValue);
                    }
                });

        // Step 1: the remembered installation, if still valid, is instant — no scan needed.
        Optional<StelvioInstallation> remembered = preferences.loadIfStillValid();
        if (remembered.isPresent()) {
            statusLabel.setText("Using previously confirmed installation:");
            selectInstallation(remembered.get());
            return;
        }

        // Step 2: nothing remembered — scan candidate folders off the JavaFX thread,
        // since filesystem access must never block the UI thread.
        statusLabel.setText("Searching common install locations...");
        runInBackground(InstallationLocator::scanCandidateLocations, this::onScanComplete);
    }

    private void onScanComplete(List<StelvioInstallation> found) {
        if (found.isEmpty()) {
            statusLabel.setText("No Stelvio installation found automatically. "
                    + "Please browse to the folder where you extracted Stelvio.");
            return;
        }
        statusLabel.setText(found.size() == 1
                ? "Found a Stelvio installation:"
                : "Found multiple Stelvio installations \u2014 select one:");
        foundInstallationsListView.getItems().setAll(found);
        foundInstallationsListView.getSelectionModel().selectFirst();
    }

    @FXML
    private void onBrowseClicked() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select your Stelvio installation folder");
        var chosen = chooser.showDialog(browseButton.getScene().getWindow());
        if (chosen == null) {
            return; // user cancelled
        }
        Optional<StelvioInstallation> validated =
                InstallationLocator.validateChosenFolder(chosen.toPath());
        if (validated.isPresent()) {
            selectInstallation(validated.get());
        } else {
            errorLabel.setText("That folder doesn't look like a Stelvio installation. "
                    + "Make sure you selected the extracted folder itself (containing "
                    + "stelvioUI.ini, problems.txt, and the bin folder) \u2014 not the .zip file.");
            continueButton.setDisable(true);
        }
    }

    private void selectInstallation(StelvioInstallation installation) {
        this.selectedInstallation = installation;
        selectedPathLabel.setText("Stelvio " + installation.version() + " \u2014 " + installation.folder());
        errorLabel.setText("");
        continueButton.setDisable(false);
    }

    @FXML
    private void onContinueClicked() {
        if (selectedInstallation == null) {
            return;
        }
        preferences.remember(selectedInstallation);
        InputScreenController nextController = SceneNavigator.showView("/fxml/input-screen.fxml");
        nextController.setInstallation(selectedInstallation);
    }

    /** Runs {@code work} off the JavaFX Application Thread, delivering the result back on it. */
    private <T> void runInBackground(Supplier<T> work, Consumer<T> onDone) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() {
                return work.get();
            }
        };
        task.setOnSucceeded(e -> onDone.accept(task.getValue()));
        task.setOnFailed(e -> errorLabel.setText("Search failed: " + task.getException().getMessage()));
        Thread thread = new Thread(task, "install-scan");
        thread.setDaemon(true);
        thread.start();
    }
}