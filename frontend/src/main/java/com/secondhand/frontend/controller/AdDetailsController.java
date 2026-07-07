package com.secondhand.frontend.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.secondhand.frontend.model.AdItem;
import com.secondhand.frontend.dto.ConversationDto;
import com.secondhand.frontend.network.NetworkClient;
import com.secondhand.frontend.util.NavigationUtils;
import com.google.gson.Gson;

public class AdDetailsController {

    @FXML private ImageView adImageView;
    @FXML private Label titleLabel;
    @FXML private Label priceLabel;
    @FXML private Label cityLabel;
    @FXML private Label categoryLabel;
    @FXML private TextArea descriptionArea;

    private AdItem currentAdvertisement;

    public void setAdData(AdItem ad) {
        this.currentAdvertisement = ad;
        titleLabel.setText(ad.getTitle());
        priceLabel.setText("$" + ad.getPrice());
        cityLabel.setText(ad.getCity());
        categoryLabel.setText(ad.getCategory());
        descriptionArea.setText(ad.getDescription());

        try {
            String imageUrl = "https://picsum.photos/400/200";
            adImageView.setImage(new Image(imageUrl, true));
        } catch (Exception e) {
            System.err.println("Could not load image.");
        }
    }

    @FXML
    public void handleBack() {
        NavigationUtils.navigateTo(titleLabel, "/com/secondhand/frontend/view/main_market.fxml", "SecondHand Market");
    }

    @FXML
    private void handleChatWithSeller() {
        if (currentAdvertisement == null) {
            System.err.println("No advertisement data available.");
            return;
        }

        // گرفتن خروجی به صورت رشته متنی جی‌سان از سرور
        String jsonResponse = NetworkClient.createConversation(Long.parseLong(currentAdvertisement.getId()));

        if (jsonResponse != null && !jsonResponse.startsWith("ERROR|")) {
            // تبدیل متن به آبجکت DTO فرانت
            Gson gson = new Gson();
            ConversationDto conversation = gson.fromJson(jsonResponse, ConversationDto.class);

            // باز کردن امن پنجره چت
            NavigationUtils.openChatBox(conversation);
        } else {
            System.err.println("Failed to start conversation: " + jsonResponse);
        }
    }
}