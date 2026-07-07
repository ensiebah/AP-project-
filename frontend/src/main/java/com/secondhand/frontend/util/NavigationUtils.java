package com.secondhand.frontend.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;

public class NavigationUtils {

    /**
     * 🔀 متد ناوبری هوشمند با قابلیت بازگرداندن کنترلر صفحه مقصد
     */
    public static <T> T navigateTo(Node node, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationUtils.class.getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) node.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();

            // 🟢 بازگرداندن کنترلر لود شده به جای void برای حل خطای اینکامپتیبل تایپ
            return loader.getController();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}