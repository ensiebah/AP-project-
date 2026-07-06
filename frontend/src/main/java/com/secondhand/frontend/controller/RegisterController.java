package com.secondhand.frontend.controller;


import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import com.secondhand.frontend.network.NetworkClient;
import com.secondhand.frontend.util.NavigationUtils;
public class RegisterController {

    @FXML private TextField fullNameField ;
    @FXML private TextField usernameField ;
    @FXML private PasswordField passwordField ;
    @FXML private TextField phoneField ;
    @FXML private TextField emailField ;
    @FXML private  Label messageLabel ;

    @FXML
    public void register(){
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();

        if (fullName.isBlank() || username.isBlank() || password.isBlank() || phone.isBlank() || email.isBlank()){
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("All fields are required.");
            return;
        }

        // ۱. ساخت بدنه به صورت JSON استاندارد منطبق بر فیلدهای بک‌آند
        String jsonRequest = String.format(
                "{\"fullName\":\"%s\",\"username\":\"%s\",\"password\":\"%s\",\"phone\":\"%s\",\"email\":\"%s\"}",
                fullName, username, password, phone, email
        );

        // ۲. ارسال مستقیم به اِندپوینت واقعی ثبت‌نام بک‌آند
        String response = NetworkClient.sendPostRequest("/users/register", jsonRequest);

        if (!response.startsWith("ERROR")){
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Registration successful! Redirecting...");

            // از آنجایی که در متد قبلی بک‌آند، خروجی لاگین‌ریسپانس را دادیم، می‌توان توکن را مستقیم هم برداشت
            // اما برای سادگی جریان فرانت، کاربر را به صفحه لاگین می‌بریم تا یوزرنیمش را بزند
            NavigationUtils.navigateTo(usernameField, "/com/secondhand/frontend/view/login.fxml", "SecondHand Market - Login");
        }
        else {
            messageLabel.setStyle("-fx-text-fill: red;");
            String errorMessage = response.contains("|") ? response.split("\\|")[1] : "Registration failed.";
            messageLabel.setText(errorMessage);
        }
    }
    @FXML
    public void goToLogin(){
        NavigationUtils.navigateTo(usernameField , "/com/secondhand/frontend/view/login.fxml" , "SecondHand Market - Login" );
    }

}