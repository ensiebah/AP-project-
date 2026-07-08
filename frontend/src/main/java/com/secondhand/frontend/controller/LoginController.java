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

        if (!response.startsWith("ERROR")) {
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("LOGIN SUCCESS!");

            if (response.contains("\"token\":\"")) {
                int tokenStartIndex = response.indexOf("\"token\":\"") + 9;
                int tokenEndIndex = response.indexOf("\"", tokenStartIndex);
                String token = response.substring(tokenStartIndex, tokenEndIndex);
                NetworkClient.authToken = token;
            }

            try {
                // 🟢 ۱. ابتدا شناسایی و ست کردن قطعی نقش کاربر در حافظه سراسری
                String upperResponse = response.toUpperCase();
                if (upperResponse.contains("ADMIN")) {
                    NetworkClient.userRole = "ADMIN";
                    System.out.println("🟢 Registered in Session: ADMIN");
                } else {
                    NetworkClient.userRole = "USER";
                    System.out.println("🔵 Registered in Session: USER");
                }

                // 🟢 ۲. حالا لود کردن صفحه بازار (در این حالت متد initialize مقدار درست را خواهد خواند)
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/secondhand/frontend/view/main_market.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("SecondHand Market - SHOP");
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Error loading main market page.");
            }

        } else {
            messageLabel.setStyle("-fx-text-fill: red;");
            String errorMessage = response.contains("|") ? response.split("\\|")[1] : "PROBLEM IN CONNECTING TO SERVER";
            messageLabel.setText(errorMessage);
        }
    }

    @FXML
    public void goToRegister() {
        NavigationUtils.navigateTo(usernameField, "/com/secondhand/frontend/view/register.fxml", "REGISTER");
    }
}