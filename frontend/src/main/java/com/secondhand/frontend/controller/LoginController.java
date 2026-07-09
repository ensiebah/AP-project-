package com.secondhand.frontend.controller;

import com.secondhand.frontend.network.NetworkClient;
import com.secondhand.frontend.util.NavigationUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    @FXML
    public void login() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isBlank() || password.isBlank()) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Username and password cannot be empty.");
            return;
        }

        String jsonRequest = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
        String response = NetworkClient.sendPostRequest("/users/login", jsonRequest);

        // 🟢 بررسی دقیق‌تر پاسخ برای تفکیک خطاها
        if (response != null && !response.startsWith("ERROR")) {
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("LOGIN SUCCESS!");

            if (response.contains("\"token\":\"")) {
                int tokenStartIndex = response.indexOf("\"token\":\"") + 9;
                int tokenEndIndex = response.indexOf("\"", tokenStartIndex);
                String token = response.substring(tokenStartIndex, tokenEndIndex);
                NetworkClient.authToken = token;
            }

            try {
                String upperResponse = response.toUpperCase();
                String fxmlPath;
                String stageTitle;

                if (upperResponse.contains("ADMIN")) {
                    NetworkClient.userRole = "ADMIN";
                    // 🟢 هدایت مستقیم به پنل مدیریت در صورت ادمین بودن
                    fxmlPath = "/com/secondhand/frontend/view/admin_panel.fxml";
                    stageTitle = "SecondHand Market - Admin Panel";
                } else {
                    NetworkClient.userRole = "USER";
                    fxmlPath = "/com/secondhand/frontend/view/main_market.fxml";
                    stageTitle = "SecondHand Market - SHOP";
                }

                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent root = loader.load();

                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle(stageTitle);
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Error loading page.");
            }

        } else {
            messageLabel.setStyle("-fx-text-fill: red;");

            // 🟢 فالبک هوشمند: اگر پایپ‌لاین درست پر شده بود متن را جدا کن، در غیر این صورت پیام خطای منطقی لاگین بده
            if (response != null && response.contains("|")) {
                messageLabel.setText(response.split("\\|")[1]);
            } else {
                // اگر سرور هیچ متنی نفرستاد یعنی احتمالاً یوزر وجود ندارد یا رمز غلط است
                messageLabel.setText("Login failed. Invalid username or password.");
            }
        }
    }

    @FXML
    public void goToRegister() {
        NavigationUtils.navigateTo(usernameField, "/com/secondhand/frontend/view/register.fxml", "REGISTER");
    }
}