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

            // ۳. استخراج توکن JWT از داخل جی‌سان پاسخ
            if (response.contains("\"token\":\"")) {
                int tokenStartIndex = response.indexOf("\"token\":\"") + 9;
                int tokenEndIndex = response.indexOf("\"", tokenStartIndex);
                String token = response.substring(tokenStartIndex, tokenEndIndex);

                // ذخیره توکن در کلاس شبکه برای استفاده در درخواست‌های بعدی
                NetworkClient.authToken = token;
            }

            // ۴. تشخیص نقش کاربر (ادمین یا عادی) از درون بدنه پاسخ برای هدایت صفحه و فعال‌سازی دکمه ادمین
            try {
                if (response.contains("\"role\":\"ADMIN\"")) {
                    // 👮‍♂️ اگر کاربر ادمین بود، ابتدا صفحه بازار عمومی را لود می‌کنیم تا دکمه پنل ادمین را برایش فعال کنیم
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/secondhand/frontend/view/main_market.fxml"));
                    Parent root = loader.load();

                    // 🟢 شکار کردن کنترلر صفحه بازار برای تزریق نقش ادمین
                    MainMarketController marketController = loader.getController();
                    marketController.configureNavigationBasedOnRole("ADMIN");

                    // نمایش صفحه بازار همراه با دکمه روشن شده ادمین
                    Stage stage = (Stage) usernameField.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("SHOP");
                    stage.show();
                } else {
                    // 🧑‍💻 اگر کاربر عادی بود، به صورت معمولی و بدون فعال شدن دکمه ادمین وارد بازار می‌شود
                    NavigationUtils.navigateTo(usernameField, "/com/secondhand/frontend/view/main_market.fxml", "SHOP");
                }
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