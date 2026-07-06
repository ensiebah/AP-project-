package com.secondhand.frontend.controller;

import com.secondhand.frontend.network.NetworkClient;
import com.secondhand.frontend.util.NavigationUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class CreateAdController {

    // 🎯 فیلدها دقیقاً با fx:id های فایل FXML شما هماهنگ شده‌اند
    @FXML private TextField titleField;
    @FXML private TextField categoryIdField;
    @FXML private TextField priceField;
    @FXML private TextField cityIdField;
    @FXML private TextArea descriptionArea;
    @FXML private Label errorLabel; // لایبل نمایش پیام‌ها که در FXML فرستادی

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

        // 🎯 اصلاح اصلی: استفاده از Locale.US برای جلوگیری از تولید کاما (,) به جای نقطه (.) در قیمت
        String jsonRequest = String.format(
                java.util.Locale.US,
                "{\"title\":\"%s\",\"description\":\"%s\",\"price\":%.2f,\"categoryId\":%d,\"cityId\":%d}",
                title, description, price, categoryId, cityId
        );

        String response = NetworkClient.sendPostRequest("/advertisements/create", jsonRequest);

        if (response != null && !response.startsWith("ERROR")) {
            errorLabel.setStyle("-fx-text-fill: green;");
            errorLabel.setText("Advertisement published successfully! Awaiting admin approval.");

            titleField.clear();
            categoryIdField.clear();
            priceField.clear();
            cityIdField.clear();
            descriptionArea.clear();
        } else {
            errorLabel.setStyle("-fx-text-fill: red;");
            // راهنمایی دقیق‌تر به کاربر در صورت بروز خطا از سمت دیتابیس
            errorLabel.setText("Failed to publish. Ensure Category/City IDs exist in database.");
        }
    }

    /**
     * 🔵 وظیفه: بازگرداندن کاربر به صفحه بازار در صورت انصراف
     * 🎯 اکشن متناظر در FXML: onAction="#goBackToMarket"
     */
    @FXML
    public void goBackToMarket() {
        // تغییر مسیر دقیقاً به پوشه view شما برای هماهنگی با کدهای پروژه
        NavigationUtils.navigateTo(titleField, "/com/secondhand/frontend/view/main_market.fxml", "SecondHand Market");
    }
}