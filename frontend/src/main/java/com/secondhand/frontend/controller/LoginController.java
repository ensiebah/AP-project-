package com.secondhand.frontend.controller;

import com.secondhand.frontend.network.NetworkClient;
import com.secondhand.frontend.util.NavigationUtils;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    public void login() {

        String username = usernameField.getText().trim();

        String password = passwordField.getText();

        if (username.isBlank() || password.isBlank()) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("username and password cannot be empty.");

            return;

        }
        String request = "LOGIN|" + username+"|"+password ;
        String response = NetworkClient.sendRequest(request) ;
        if(response.startsWith("LOGIN_SUCCESS")) {
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("LOGIN_SUCCESS!");

            String[] parts = response.split("\\|");
            String role = parts.length > 1 ? parts[1] : "USER";


            if ("ADMIN".equalsIgnoreCase(role)) {
                NavigationUtils.navigateTo(usernameField, "/com/secondhand/frontend/view/admin_dashboard.fxml", "MANGING PANEL");

            } else {
                NavigationUtils.navigateTo(usernameField, "/com/secondhand/frontend/view/main_market.fxml", "SHOP");
            }
        } else {
            messageLabel.setStyle("-fx-text-fill: red;");
            if (response.contains("|")){
                messageLabel.setText(response.split("\\|")[1]);
            }else {
                messageLabel.setText("PROBLEM IN CONNECTING TO SERVER");
            }
        }


    }


    @FXML
    public void goToRegister() {

        NavigationUtils.navigateTo(usernameField, "/com/secondhand/frontend/view/register.fxml", "REGISTER");

    }

}