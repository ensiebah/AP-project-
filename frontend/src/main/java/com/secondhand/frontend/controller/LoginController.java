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
            messageLabel.setText("Username and password cannot be empty.");
            return;
        }

        // ۱. ساخت جی‌سان درخواست ورود
        String jsonRequest = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);

        // ۲. ارسال درخواست به اِندپوینت واقعی لاگین بک‌آند
        String response = NetworkClient.sendPostRequest("/users/login", jsonRequest);

        if (!response.startsWith("ERROR")) {
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("LOGIN SUCCESS!");

            // ۳. استخراج توکن JWT از داخل جی‌سان پاسخ (پاسخ بک‌آند حاوی فیلد "token" است)
            // برای سادگی بدون کتابخانه سنگین، با ساب‌استرینگ توکن را برمی‌داریم:
            if (response.contains("\"token\":\"")) {
                int tokenStartIndex = response.indexOf("\"token\":\"") + 9;
                int tokenEndIndex = response.indexOf("\"", tokenStartIndex);
                String token = response.substring(tokenStartIndex, tokenEndIndex);

                // ذخیره توکن در کلاس شبکه برای استفاده در درخواست‌های بعدی (مثل ثبت آگهی یا چت)
                NetworkClient.authToken = token;
            }

            // ۴. تشخیص نقش کاربر (ادمین یا عادی) از درون بدنه پاسخ برای هدایت صفحه
            if (response.contains("\"role\":\"ADMIN\"")) {
                NavigationUtils.navigateTo(usernameField, "/com/secondhand/frontend/view/admin_dashboard.fxml", "MANAGING PANEL");
            } else {
                NavigationUtils.navigateTo(usernameField, "/com/secondhand/frontend/view/main_market.fxml", "SHOP");
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