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

/**
 * Note: this controller never auto-skips itself even when a remembered
 * installation is still valid - that decision (skip straight to the input
 * screen) is made once, upstream, by MainApp at startup. Any time this
 * screen IS shown - whether on first run or via the input screen's
 * "Change Stelvio folder..." button - it always renders normally, just
 * choosing which of two states to show: "confirmed" (a valid install is
 * already known) or "searching" (nothing confirmed yet, or the user asked
 * to look again).
 */
public class InstallScreenController {

    @FXML private Label statusLabel;
    @FXML private ListView<StelvioInstallation> foundInstallationsListView;
    @FXML private Label selectedPathLabel;
    @FXML private Label errorLabel;
    @FXML private Button rescanButton;
    @FXML private Button browseButton;
    @FXML private Button continueButton;

    private final InstallationPreferences preferences = new InstallationPreferences();
    private StelvioInstallation selectedInstallation;

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

        Optional<StelvioInstallation> remembered = preferences.loadIfStillValid();
        if (remembered.isPresent()) {
            showConfirmedState(remembered.get());
        } else {
            showSearchingState();
        }
    }

    /** A valid installation is already known - show it plainly, hide the search UI. */
    private void showConfirmedState(StelvioInstallation installation) {
        selectInstallation(installation);
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
        foundInstallationsListView.setVisible(false);
        foundInstallationsListView.setManaged(false);
    }

    /** Nothing confirmed (or the user asked to look again) - run the scan, show the list. */
    private void showSearchingState() {
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
        foundInstallationsListView.setVisible(true);
        foundInstallationsListView.setManaged(true);
        statusLabel.setText("Searching common install locations...");
        runInBackground(InstallationLocator::scanCandidateLocations, this::onScanComplete);
    }

    @FXML
    private void onRescanClicked() {
        showSearchingState();
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
        MainShellController shellController = SceneNavigator.showView("/fxml/main-shell.fxml");
        shellController.setInstallation(selectedInstallation);
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