package com.github.adamqyang.ui;

import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        SceneNavigator.attachTo(stage);
        SceneNavigator.showView("/fxml/install-screen.fxml");
        stage.setTitle("Stelvio GUI");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}