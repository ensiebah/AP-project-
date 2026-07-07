package com.secondhand.frontend.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.secondhand.frontend.model.AdItem;
import com.secondhand.frontend.util.NavigationUtils;
import com.secondhand.frontend.network.NetworkClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class AdDetailsController {

    @FXML private ImageView adImageView;
    @FXML private Label titleLabel;
    @FXML private Label priceLabel;
    @FXML private Label cityLabel;
    @FXML private Label categoryLabel;
    @FXML private TextArea descriptionArea;

    // 🟢 فیلدهای گرافیکی جدید برای پشتیبانی از داک امتیازدهی
    @FXML private Label sellerLabel;
    @FXML private Label ratingLabel;

    private AdItem currentAd;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void setAdData(AdItem ad) {
        this.currentAd = ad;
        titleLabel.setText(ad.getTitle());
        priceLabel.setText("$" + ad.getPrice());
        cityLabel.setText(ad.getCity());
        categoryLabel.setText(ad.getCategory());
        descriptionArea.setText(ad.getDescription());

        if (sellerLabel != null) {
            sellerLabel.setText("Seller: " + ad.getSellerName());
        }

        try {
            String imageUrl = "https://picsum.photos/400/200";
            adImageView.setImage(new Image(imageUrl, true));
        } catch (Exception e) {
            System.err.println("Could not load image.");
        }

        // 🟢 لود کردن میانگین امتیاز فروشنده از بک‌اَند به محض باز شدن صفحه
        loadSellerAverageRating(ad.getSellerId());
    }

    /**
     * 🔄 دریافت میانگین امتیاز واقعی فروشنده از بک‌اَند
     */
    private void loadSellerAverageRating(Long sellerId) {
        if (sellerId == null || ratingLabel == null) return;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/ratings/seller/" + sellerId + "/average"))
                .header("Authorization", "Bearer " + NetworkClient.authToken)
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(scoreStr -> Platform.runLater(() -> {
                    try {
                        double score = Double.parseDouble(scoreStr);
                        ratingLabel.setText(String.format("Rating: %.1f / 5.0", score));
                    } catch (Exception e) {
                        ratingLabel.setText("Rating: No ratings yet");
                    }
                }));
    }

    /**
     * ⭐ اکشن دکمه امتیازدهی به فروشنده (متصل به کامپوننت داک پروژه)
     */
    @FXML
    public void handleRateSeller() {
        if (currentAd == null) return;

        // ۱. گرفتن امتیاز عددی از کاربر
        TextInputDialog scoreDialog = new TextInputDialog("5");
        scoreDialog.setTitle("Rate Seller");
        scoreDialog.setHeaderText("Enter a score between 1 and 5 for " + currentAd.getSellerName());
        scoreDialog.setContentText("Score (1-5):");

        Optional<String> scoreResult = scoreDialog.showAndWait();
        if (scoreResult.isEmpty()) return; // لغو عملیات

        int score;
        try {
            score = Integer.parseInt(scoreResult.get().trim());
            if (score < 1 || score > 5) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Score", "Score must be an integer between 1 and 5.");
            return;
        }

        // ۲. گرفتن کامنت اختیاری از کاربر
        TextInputDialog commentDialog = new TextInputDialog("");
        commentDialog.setTitle("Seller Feedback");
        commentDialog.setHeaderText("Leave an optional comment for the seller");
        commentDialog.setContentText("Comment:");

        Optional<String> commentResult = commentDialog.showAndWait();
        String comment = commentResult.orElse("").trim();

        // ۳. ساخت آبجکت جی‌سان هماهنگ با RatingDto بک‌اَند شما
        // نکته: خریدار همان کاربری است که با توکن لاگین کرده، پس ارسال بویر‌آیدی در درخواست‌های کلاینت-سرور واقعی
        // معمولاً در سرور از روی توکن استخراج می‌شود، اما چون در متد کنترلر شما قید شده، یک مقدار موقت می‌فرستیم یا سرور خودش هندل می‌کند.
        String jsonBody = String.format(
                "{\"advertisementId\":%s,\"score\":%d,\"comment\":\"%s\"}",
                currentAd.getId(), score, comment
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/ratings"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + NetworkClient.authToken)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.statusCode() == 200 || response.statusCode() == 201) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Rating submitted successfully!");
                        loadSellerAverageRating(currentAd.getSellerId()); // آپدیت آنی ستاره‌ها روی صفحه
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Failure", "Could not submit rating. " + response.body());
                    }
                }));
    }

    @FXML
    public void handleBack() {
        NavigationUtils.navigateTo(titleLabel, "/com/secondhand/frontend/view/main_market.fxml", "SecondHand Market");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}