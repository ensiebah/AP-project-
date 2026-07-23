package com.secondhand.frontend;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.secondhand.frontend.util.UiTheme;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader =
                new FXMLLoader(getClass().getResource("/com/secondhand/frontend/view/login.fxml"));

        javafx.scene.Parent root = loader.load();
        UiTheme.decorate(root);
        Scene scene = new Scene(root);

        stage.setTitle("SecondHand Market");
        stage.setScene(scene);
        stage.setMaximized(true); // تمام‌صفحه شدن

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}