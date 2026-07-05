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
        String fullName = fullNameField.getText().trim() ;
        String username = usernameField.getText().trim() ;
        String password = passwordField.getText() ;
        String phone = phoneField.getText().trim() ;
        String email = emailField.getText().trim() ;

        if (fullName.isBlank() || username.isBlank() || password.isBlank() || phone.isBlank() || email.isBlank()){
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("All fields are required.");
            return;
        }

        String request = "REGISTER|" + username + "|" + password + "|" + fullName + "|" + phone + "|" + email;
        String response = NetworkClient.sendRequest(request) ;

        if (response.startsWith("REGISTER_SUCCESS")){
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Registration successful! Redirecting...");

            NavigationUtils.navigateTo(usernameField ,"/com/secondhand/frontend/view/login.fxml" , "SecondHand Market - Login" );

        }
        else {
            messageLabel.setStyle("-fx-text-fill: red;");
            if (response.contains("|")){
                messageLabel.setText(response.split("\\|")[1]);

            }else {
                messageLabel.setText("Registration failed.");
            }
        }
    }
    @FXML
    public void goToLogin(){
        NavigationUtils.navigateTo(usernameField , "/com/secondhand/frontend/view/login.fxml" , "SecondHand Market - Login" );
    }

}
