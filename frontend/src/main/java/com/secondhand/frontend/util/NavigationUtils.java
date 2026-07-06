package com.secondhand.frontend.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.stage.Stage;
import java.io.IOException;

public class NavigationUtils {
    public static void navigateTo(Control control , String fxmlPath , String title){
        try{
            Stage stage = (Stage) control.getScene().getWindow() ;
            Parent root = FXMLLoader.load(NavigationUtils.class.getResource(fxmlPath)) ;
            stage.setScene(new Scene(root)) ;
            stage.setTitle(title);
            stage.show();


        }catch (IOException e){
            System.err.println("ERROR IN LOADING FXML FILE :"+fxmlPath);
            e.printStackTrace();
        }

    }
}