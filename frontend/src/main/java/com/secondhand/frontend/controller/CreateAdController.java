package com.secondhand.frontend.controller;

import com.secondhand.frontend.network.NetworkClient;
import com.secondhand.frontend.util.NavigationUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class CreateAdController {

    // 🎯 فیلدها دقیقاً با fx:id های فایل FXML شما هماهنگ هستند
    @FXML private TextField titleField;
    @FXML private TextField categoryIdField;
    @FXML private TextField priceField;
    @FXML private TextField cityIdField;
    @FXML private TextArea descriptionArea;
    @FXML private Label errorLabel; // لایبل نمایش پیام‌ها

    /**
     * 🟢 وظیفه: جمع‌آوری داده‌ها، تبدیل به JSON و ارسال به بک‌آند هنگام کلیک روی Publish Ad
     * 🎯 اکشن متناظر در FXML: onAction="#handleSaveAdvertisement"
     */
    @FXML
    public void handleSaveAdvertisement() {
        String title = titleField.getText().trim();
        String categoryIdText = categoryIdField.getText().trim();
        String priceText = priceField.getText().trim();
        String cityIdText = cityIdField.getText().trim();
        String description = descriptionArea.getText().trim();

        if (title.isBlank() || categoryIdText.isBlank() || priceText.isBlank() || cityIdText.isBlank() || description.isBlank()) {
            errorLabel.setStyle("-fx-text-fill: red;");
            errorLabel.setText("Please fill in all fields.");
            return;
        }

        long categoryId;
        long cityId;
        double price;

        try {
            categoryId = Long.parseLong(categoryIdText);
            cityId = Long.parseLong(cityIdText);
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            errorLabel.setStyle("-fx-text-fill: red;");
            errorLabel.setText("Category ID, City ID, and Price must be valid numbers.");
            return;
        }

        // 💡 اصلاح کلیدی: ارسال لینک عکس پیش‌فرض برای حل باگ لود نشدن جزئیات آگهی
        String defaultImageUrl = "https://picsum.photos/400/200";

        // استفاده از Locale.US برای جلوگیری از تولید کاما (,) به جای نقطه (.) در ویندوزهای فارسی/اروپایی
        String jsonRequest = String.format(
                java.util.Locale.US,
                "{\"title\":\"%s\",\"description\":\"%s\",\"price\":%.2f,\"categoryId\":%d,\"cityId\":%d,\"imageUrl\":\"%s\"}",
                title, description, price, categoryId, cityId, defaultImageUrl
        );

        String response = NetworkClient.sendPostRequest("/advertisements/create", jsonRequest);

        if (response != null && !response.startsWith("ERROR")) {
            errorLabel.setStyle("-fx-text-fill: green;");
            errorLabel.setText("Advertisement published successfully! Awaiting admin approval.");

            // پاک کردن فرم پس از موفقیت
            titleField.clear();
            categoryIdField.clear();
            priceField.clear();
            cityIdField.clear();
            descriptionArea.clear();
        } else {
            errorLabel.setStyle("-fx-text-fill: red;");
            errorLabel.setText("Failed to publish. Ensure Category/City IDs exist in database.");
        }
    }

    /**
     * 🔵 وظیفه: بازگرداندن کاربر به صفحه بازار در صورت انصراف
     * 🎯 اکشن متناظر در FXML: onAction="#goBackToMarket"
     */
    @FXML
    public void goBackToMarket() {
        NavigationUtils.navigateTo(titleField, "/com/secondhand/frontend/view/main_market.fxml", "SecondHand Market");
    }
}