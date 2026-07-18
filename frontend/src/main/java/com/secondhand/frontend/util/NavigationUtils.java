package com.secondhand.frontend.util;

import com.secondhand.frontend.dto.ConversationDto;
import com.secondhand.frontend.controller.ChatController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.stage.Stage;
import java.io.IOException;

public class NavigationUtils {

    public static <T> T navigateTo(Control control, String fxmlPath, String title) {
        try {
            // 🟢 بخش ایمن‌سازی: ابتدا تلاش برای گرفتن استیج از کنترل، و در صورت نال بودن، پیدا کردن پنجره فعال برنامه
            Stage stage = null;
            if (control != null && control.getScene() != null) {
                stage = (Stage) control.getScene().getWindow();
            }

            if (stage == null) {
                stage = javafx.stage.Window.getWindows().stream()
                        .filter(Stage.class::isInstance)
                        .map(Stage.class::cast)
                        .filter(Stage::isShowing)
                        .findFirst()
                        .orElse(null);
            }

            if (stage == null) {
                System.err.println("ERROR: No active stage found for navigation!");
                return null;
            }

            // 🔵 حفظ دقیق تمام کدهای اصلی شما بدون هیچ تغییری
            FXMLLoader loader = new FXMLLoader(NavigationUtils.class.getResource(fxmlPath));
            Parent root = loader.load();

            String cssPath = NavigationUtils.class.getResource("/style.css").toExternalForm();
            root.getStylesheets().add(cssPath);

            if (stage.getScene() != null) {
                stage.getScene().setRoot(root);
            } else {
                Scene scene = new Scene(root);
                stage.setScene(scene);
            }

            stage.setTitle(title);

            // 🟢 اطمینان از اینکه بعد از هر تغییر صفحه، استیج همچنان ماکسیمایز (تمام‌صفحه) باقی می‌ماند
            stage.setMaximized(true);

            stage.show();

            return loader.getController();
        } catch (IOException e) {
            System.err.println("ERROR IN LOADING FXML FILE :" + fxmlPath);
            e.printStackTrace();
            return null;
        }
    }

    public static void openChatBox(ConversationDto conversation) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationUtils.class.getResource("/com/secondhand/frontend/view/chat_box.fxml"));
            Parent root = loader.load();

            ChatController controller = loader.getController();
            controller.setConversationData(conversation);

            Stage stage = new Stage();
            stage.setTitle("Chat Room - " + conversation.getAdvertisementTitle());

            // 🟢 اضافه کردن استایل CSS به ریشه پنجره چت
            String cssPath = NavigationUtils.class.getResource("/style.css").toExternalForm();
            root.getStylesheets().add(cssPath);

            Scene scene = new Scene(root);
            stage.setScene(scene);

            stage.setOnCloseRequest(event -> controller.shutdown());
            stage.show();
        } catch (IOException e) {
            System.err.println("ERROR IN LOADING CHAT FXML FILE!");
            e.printStackTrace();
        }
    }
}