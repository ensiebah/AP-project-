package com.secondhand.frontend.util;

import com.secondhand.frontend.dto.ConversationDto;
import com.secondhand.frontend.controller.ChatController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.stage.Stage;
import java.io.IOException;

public class NavigationUtils {

    // متد قبلی خودت (بدون هیچ تغییری)
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

    // 👈 متد جدیدی که باید اضافه کنی تا پنجره چت را باز کند
    public static void openChatBox(ConversationDto conversation) {
        try {
            // ۱. ساخت لودر برای فایل fxml چت‌باکس
            FXMLLoader loader = new FXMLLoader(NavigationUtils.class.getResource("/com/secondhand/frontend/view/chat_box.fxml"));
            Parent root = loader.load();

            // ۲. بیرون کشیدن کنترلر صفحه چت‌باکس جهت پاس دادن اطلاعات مکالمه
            ChatController controller = loader.getController();
            controller.setConversationData(conversation);

            // ۳. باز کردن چت در یک پنجره (Stage) جدید و مستقل
            Stage stage = new Stage();
            stage.setTitle("Chat Room - " + conversation.getAdvertisementTitle());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            System.err.println("ERROR IN LOADING CHAT FXML FILE!");
            e.printStackTrace();
        }
    }
}