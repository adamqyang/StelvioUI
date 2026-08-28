package com.github.adamqyang.ui;

import java.util.Optional;

import com.github.adamqyang.install.InstallationPreferences;
import com.github.adamqyang.install.StelvioInstallation;

import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        SceneNavigator.attachTo(stage);

        // Skip straight past the install screen if we already have a confirmed,
        // still-valid installation - no need to make the user click through it
        // every launch. InstallScreenController itself never auto-skips (see its
        // own initialize()), so navigating there deliberately - e.g. via the
        // input screen's "Change Stelvio folder..." button - always shows the
        // full picker/confirm UI rather than bouncing straight back here.
        Optional<StelvioInstallation> remembered = new InstallationPreferences().loadIfStillValid();
        if (remembered.isPresent()) {
            MainShellController controller = SceneNavigator.showView("/fxml/main-shell.fxml");
            controller.setInstallation(remembered.get());
        } else {
            SceneNavigator.showView("/fxml/install-screen.fxml");
        }

        stage.setTitle("Stelvio GUI");
        stage.setMinWidth(600);
        stage.setMinHeight(500);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}