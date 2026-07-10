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
            Stage stage = (Stage) control.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(NavigationUtils.class.getResource(fxmlPath));
            Parent root = loader.load();

            stage.setScene(new Scene(root));
            stage.setTitle(title);
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
            stage.setScene(new Scene(root));

            // 🟢 Shuts down background polling thread immediately when the chat popup is closed
            stage.setOnCloseRequest(event -> controller.shutdown());

            stage.show();
        } catch (IOException e) {
            System.err.println("ERROR IN LOADING CHAT FXML FILE!");
            e.printStackTrace();
        }
    }
}