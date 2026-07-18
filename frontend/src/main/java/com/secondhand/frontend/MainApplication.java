package com.secondhand.frontend;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader =
                new FXMLLoader(getClass().getResource("/com/secondhand/frontend/view/login.fxml"));

        Scene scene = new Scene(loader.load());

        // 🟢 لود کردن استایل به صورت سراسری روی کل پنجره برنامه
        String cssPath = getClass().getResource("/style.css").toExternalForm();
        scene.getStylesheets().add(cssPath);

        stage.setTitle("SecondHand Market");
        stage.setScene(scene);
        stage.setMaximized(true); // تمام‌صفحه شدن

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}