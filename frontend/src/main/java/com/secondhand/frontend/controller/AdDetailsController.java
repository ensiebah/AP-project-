package com.secondhand.frontend.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.secondhand.frontend.model.AdItem;
import com.secondhand.frontend.util.NavigationUtils;

public class AdDetailsController {

    @FXML private ImageView adImageView;
    @FXML private Label titleLabel;
    @FXML private Label priceLabel;
    @FXML private Label cityLabel;
    @FXML private Label categoryLabel;
    @FXML private TextArea descriptionArea;

    // This method will be called by MainMarketController when an ad is clicked
    public void setAdData(AdItem ad) {
        titleLabel.setText(ad.getTitle());
        priceLabel.setText("$" + ad.getPrice());
        cityLabel.setText(ad.getCity());
        categoryLabel.setText(ad.getCategory());
        descriptionArea.setText(ad.getDescription());


        try {
            String imageUrl = "https://picsum.photos/400/200"; // Placeholder image URL
            adImageView.setImage(new Image(imageUrl, true));
        } catch (Exception e) {
            System.err.println("Could not load image.");
        }
    }

    @FXML
    public void handleBack() {
        NavigationUtils.navigateTo(titleLabel, "/com/secondhand/frontend/view/main_market.fxml", "SecondHand Market");
    }
}