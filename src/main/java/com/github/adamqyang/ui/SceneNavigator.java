package com.github.adamqyang.ui;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Centralizes swapping the primary stage's content between screens, so
 * individual controllers don't need to know about Stage/Scene management —
 * they just call {@link #showView(String)} with the next FXML to display.
 */
public final class SceneNavigator {

    private static Stage primaryStage;

    private SceneNavigator() {
    }

    /** Call once, from MainApp.start(), before showing the first view. */
    public static void attachTo(Stage stage) {
        primaryStage = stage;
    }

    /**
     * Loads the given FXML resource (classpath-relative, e.g.
     * "/fxml/install-screen.fxml") and displays it, returning its controller
     * in case the caller needs to pass it initial data.
     */
    public static <T> T showView(String fxmlResourcePath) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlResourcePath));
            Parent root = loader.load();
            if (primaryStage.getScene() == null) {
                primaryStage.setScene(new Scene(root, 800, 600));
            } else {
                primaryStage.getScene().setRoot(root);
            }
            return loader.getController();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load view: " + fxmlResourcePath, e);
        }
    }
}
