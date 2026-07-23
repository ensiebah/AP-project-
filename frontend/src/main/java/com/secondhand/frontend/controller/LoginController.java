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
import javafx.scene.control.Button;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.json.JSONObject;
import com.secondhand.frontend.util.UiTheme;
import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private Button loginButton;
    @FXML private Button registerButton;

    @FXML
    public void login() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isBlank() || password.isBlank()) {
            showMessage("Username and password cannot be empty.", "-fx-text-fill: #d95362;");
            return;
        }

        setLoginBusy(true);
        showMessage("Signing you in…", "-fx-text-fill: #287de0;");
        String jsonRequest = String.format(
                "{\"username\":\"%s\",\"password\":\"%s\"}", username, password
        );

        Thread loginThread = new Thread(() -> {
            String response = NetworkClient.sendPostRequest("/users/login", jsonRequest);
            Platform.runLater(() -> completeLogin(response));
        }, "login-request-thread");
        loginThread.setDaemon(true);
        loginThread.start();
    }

    private void completeLogin(String response) {
        setLoginBusy(false);
        if (response == null || response.startsWith("ERROR")) {
            if (response != null && response.contains("|")) {
                showMessage(response.split("\\|")[1], "-fx-text-fill: #d95362;");
            } else {
                showMessage("Login failed. Invalid username or password.", "-fx-text-fill: #d95362;");
            }
            return;
        }

        try {
            JSONObject loginData = new JSONObject(response);
            NetworkClient.authToken = loginData.optString("token", null);
            NetworkClient.currentUsername = loginData.optString("username", "Guest");
            NetworkClient.currentFullName = loginData.optString(
                    "fullName", NetworkClient.currentUsername
            );

            String role = loginData.optString("role", "USER");
            String fxmlPath;
            String stageTitle;
            if ("ADMIN".equalsIgnoreCase(role)) {
                NetworkClient.userRole = "ADMIN";
                fxmlPath = "/com/secondhand/frontend/view/admin_panel.fxml";
                stageTitle = "SecondHand Market - Admin Panel";
            } else {
                NetworkClient.userRole = "USER";
                fxmlPath = "/com/secondhand/frontend/view/main_market.fxml";
                stageTitle = "SecondHand Market";
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            UiTheme.decorate(root);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(stageTitle);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Could not open the marketplace after login.", "-fx-text-fill: #d95362;");
        }
    }

    private void setLoginBusy(boolean busy) {
        usernameField.setDisable(busy);
        passwordField.setDisable(busy);
        if (loginButton != null) {
            loginButton.setDisable(busy);
            loginButton.setText(busy ? "Signing in…" : "Sign in");
        }
        if (registerButton != null) {
            registerButton.setDisable(busy);
        }
    }

    private void showMessage(String text, String style) {
        messageLabel.setStyle(style);
        messageLabel.setText(text);
    }

    @FXML
    public void goToRegister() {
        NavigationUtils.navigateTo(usernameField, "/com/secondhand/frontend/view/register.fxml", "REGISTER");
    }
}