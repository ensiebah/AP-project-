package com.secondhand.frontend.controller;

import com.secondhand.frontend.network.NetworkClient;
import com.secondhand.frontend.util.NavigationUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

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

            // 🟢 ۱. ناوبری استاندارد و یکپارچه به صفحه مارکت با استفاده از ابزار ناوبری خودتان
            MainMarketController marketController = NavigationUtils.navigateTo(
                    usernameField,
                    "/com/secondhand/frontend/view/main_market.fxml",
                    "SecondHand Market - SHOP"
            );

            // 🟢 ۲. بررسی هوشمند نقش کاربر و اعمال روی کنترلر مارکت جهت رفع قاطع باگ دکمه ادمین
            if (marketController != null) {
                if (response.contains("\"role\":\"ADMIN\"")) {
                    marketController.configureNavigationBasedOnRole("ADMIN");
                } else {
                    marketController.configureNavigationBasedOnRole("USER");
                }
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