package com.secondhand.frontend.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.secondhand.frontend.model.AdItem;
import com.secondhand.frontend.util.NavigationUtils;
import com.secondhand.frontend.network.NetworkClient;
import com.secondhand.frontend.dto.ConversationDto;
import javafx.scene.layout.StackPane;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdDetailsController {

    @FXML private ImageView adImageView;
    @FXML private Label titleLabel;
    @FXML private Label priceLabel;
    @FXML private Label cityLabel;
    @FXML private Label categoryLabel;
    @FXML private TextArea descriptionArea;
    @FXML private Label sellerLabel;
    @FXML private Label ratingLabel;
    @FXML private Button btnFavorite;
    @FXML private ListView<String> commentsListView;
    @FXML private TextField newCommentField;
    @FXML private Button btnTabProductComments;
    @FXML private Button btnTabSellerRatings;

    // اجزای جدید FXML برای کنترل کاروسل مالتی‌مدیا
    @FXML private StackPane imageContainer;
    @FXML private Button btnPrevImage;
    @FXML private Button btnNextImage;

    // متغیرهای فرانت‌اند جهت پیمایش لیست تصاویر
    private final List<String> imageList = new ArrayList<>();
    private int currentImageIndex = 0;

    private boolean showingProductComments = true;
    private AdItem currentAd;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private boolean isFavorite = false;

    public void setAdData(AdItem ad) {
        this.currentAd = ad;
        titleLabel.setText(ad.getTitle());
        priceLabel.setText("$" + ad.getPrice());
        cityLabel.setText(ad.getCity());
        categoryLabel.setText(ad.getCategory());

        String rawDescription = ad.getDescription() != null ? ad.getDescription() : "";
        String cleanDescription = rawDescription;

        imageList.clear();

        if (rawDescription.contains("[IMG_URL:")) {
            int start = rawDescription.indexOf("[IMG_URL:") + 9;
            int end = rawDescription.indexOf("]", start);
            if (end > start) {
                String allUrls = rawDescription.substring(start, end);
                String[] splitUrls = allUrls.split(",");
                for (String url : splitUrls) {
                    if (!url.isBlank()) {
                        imageList.add(url.trim());
                    }
                }
                cleanDescription = rawDescription.substring(0, rawDescription.indexOf("[IMG_URL:")).trim();
            }
        } else if (rawDescription.contains("http")) {
            imageList.add(rawDescription.trim());
        }

        descriptionArea.setText(cleanDescription);

        if (sellerLabel != null) {
            sellerLabel.setText("Seller: " + ad.getSellerName());
        }

        if (!imageList.isEmpty()) {
            currentImageIndex = 0;
            displayCurrentImage();
        } else {
            try {
                String fallbackPath = getClass().getResource("/com/secondhand/frontend/images/default-ad.png") != null ?
                        getClass().getResource("/com/secondhand/frontend/images/default-ad.png").toExternalForm() :
                        "https://picsum.photos/400/200";
                adImageView.setImage(new Image(fallbackPath, true));
            } catch (Exception e) {
                adImageView.setImage(new Image("https://picsum.photos/400/200", true));
            }
        }

        // فعال‌سازی پایشگر حرکت و کلیک روی عکس
        setupCarouselHoverLogic();

        loadSellerAverageRating(ad.getSellerId());
        loadSellerComments(ad.getSellerId());
        loadProductComments(Long.parseLong(ad.getId().trim()));
        switchTabToProductComments();
        checkFavoriteStatus();
    }

    private void displayCurrentImage() {
        if (imageList.isEmpty()) return;
        try {
            adImageView.setImage(new Image(imageList.get(currentImageIndex), true));
        } catch (Exception e) {
            adImageView.setImage(new Image("https://picsum.photos/400/200", true));
        }
    }

    /**
     * 🟢 متد بهینه‌شده برای مدیریت حرکت ماوس و کلیک روی لبه‌های چپ و راست کانتینر تصویر
     */
    private void setupCarouselHoverLogic() {
        if (imageContainer == null) return;

        // مدیریت تغییر وضعیت فلش‌ها بر اساس هوور ماوس
        imageContainer.setOnMouseMoved(event -> {
            double mouseX = event.getX();
            double containerWidth = imageContainer.getWidth();

            if (mouseX < containerWidth * 0.4 && currentImageIndex > 0) {
                btnPrevImage.setVisible(true);
            } else {
                btnPrevImage.setVisible(false);
            }

            if (mouseX > containerWidth * 0.6 && currentImageIndex < imageList.size() - 1) {
                btnNextImage.setVisible(true);
            } else {
                btnNextImage.setVisible(false);
            }
        });

        imageContainer.setOnMouseExited(event -> {
            if (btnPrevImage != null) btnPrevImage.setVisible(false);
            if (btnNextImage != null) btnNextImage.setVisible(false);
        });

        // 🟢 قابلیت جدید: کلیک روی سمت راست یا چپ باکس تصویر برای رفتن به بعدی/قبلی
        imageContainer.setOnMouseClicked(event -> {
            double mouseX = event.getX();
            double containerWidth = imageContainer.getWidth();

            if (mouseX > containerWidth * 0.5) {
                handleNextImage(); // کلیک روی نیمه سمت راست
            } else {
                handlePrevImage(); // کلیک روی نیمه سمت چپ
            }
        });
    }

    @FXML
    public void handleNextImage() {
        if (currentImageIndex < imageList.size() - 1) {
            currentImageIndex++;
            displayCurrentImage();
            btnNextImage.setVisible(currentImageIndex < imageList.size() - 1);
            btnPrevImage.setVisible(true);
        }
    }

    @FXML
    public void handlePrevImage() {
        if (currentImageIndex > 0) {
            currentImageIndex--;
            displayCurrentImage();
            btnPrevImage.setVisible(currentImageIndex > 0);
            btnNextImage.setVisible(true);
        }
    }

    private void loadSellerAverageRating(Long sellerId) {
        if (sellerId == null || ratingLabel == null) return;

        String token = NetworkClient.authToken != null ? NetworkClient.authToken : "";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/ratings/seller/" + sellerId + "/average"))
                .header("Authorization", "Bearer " + token)
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

    private void loadSellerComments(Long sellerId) {
        if (sellerId == null || commentsListView == null) return;

        String token = NetworkClient.authToken != null ? NetworkClient.authToken : "";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/ratings/seller/" + sellerId))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(jsonResponse -> Platform.runLater(() -> {
                    try {
                        commentsListView.getItems().clear();
                        if (jsonResponse.startsWith("ERROR")) return;

                        JSONArray arr = new JSONArray(jsonResponse);
                        if (arr.length() == 0) {
                            commentsListView.getItems().add("No reviews left for this seller yet.");
                            return;
                        }

                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            String buyer = obj.optString("buyerUsername", "Anonymous");
                            int score = obj.optInt("score", 5);
                            String comment = obj.optString("comment", "");

                            String row = String.format("⭐ [%d/5] %s: %s", score, buyer, comment.isBlank() ? "(No text)" : comment);
                            commentsListView.getItems().add(row);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }));
    }

    @FXML
    public void handleToggleFavorite() {
        if (currentAd == null) return;
        Long adId = Long.parseLong(currentAd.getId().trim());

        Thread favThread = new Thread(() -> {
            if (isFavorite) {
                String response = NetworkClient.removeFavorite(adId);
                Platform.runLater(() -> {
                    if (!response.startsWith("ERROR")) {
                        isFavorite = false;
                        btnFavorite.setText("♥ Save Ad");
                        btnFavorite.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                        showAlert(Alert.AlertType.INFORMATION, "Favorites", "Removed from favorites.");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to remove from favorites.");
                    }
                });
            } else {
                String response = NetworkClient.addFavorite(adId);
                Platform.runLater(() -> {
                    if (!response.startsWith("ERROR")) {
                        isFavorite = true;
                        btnFavorite.setText("♥ Saved");
                        btnFavorite.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;");
                        showAlert(Alert.AlertType.INFORMATION, "Favorites", "Added to favorites!");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to add to favorites.");
                    }
                });
            }
        });
        favThread.setDaemon(true);
        favThread.start();
    }

    private void checkFavoriteStatus() {
        if (currentAd == null) return;
        Thread checkThread = new Thread(() -> {
            String response = NetworkClient.getMyFavorites();
            if (response != null && !response.startsWith("ERROR")) {
                try {
                    JSONArray arr = new JSONArray(response);
                    Long adId = Long.parseLong(currentAd.getId().trim());
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        if (obj.getLong("advertisementId") == adId) {
                            isFavorite = true;
                            break;
                        }
                    }
                    Platform.runLater(() -> {
                        if (isFavorite) {
                            btnFavorite.setText("♥ Saved");
                            btnFavorite.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;");
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        checkThread.setDaemon(true);
        checkThread.start();
    }

    @FXML
    public void handleRateSeller() {
        if (currentAd == null) return;
        Long adId = Long.parseLong(currentAd.getId().trim());

        Thread checkEligibilityThread = new Thread(() -> {
            String response = NetworkClient.checkRatingEligibility(adId);
            Platform.runLater(() -> {
                if (response == null || response.startsWith("ERROR")) {
                    showAlert(Alert.AlertType.ERROR, "Network Error", "Could not verify rating status.");
                    return;
                }

                try {
                    JSONObject json = new JSONObject(response);
                    boolean allowed = json.getBoolean("allowed");

                    if (!allowed) {
                        String reason = json.optString("reason", "You are not allowed to rate this advertisement.");
                        showAlert(Alert.AlertType.WARNING, "Rating Denied", reason);
                        return;
                    }

                    openRatingDialogs();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
        checkEligibilityThread.setDaemon(true);
        checkEligibilityThread.start();
    }

    private void openRatingDialogs() {
        TextInputDialog scoreDialog = new TextInputDialog("5");
        scoreDialog.setTitle("Rate Seller");
        scoreDialog.setHeaderText("Enter a score between 1 and 5 for " + currentAd.getSellerName());
        scoreDialog.setContentText("Score (1-5):");

        Optional<String> scoreResult = scoreDialog.showAndWait();
        if (scoreResult.isEmpty()) return;

        int score;
        try {
            score = Integer.parseInt(scoreResult.get().trim());
            if (score < 1 || score > 5) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Score", "Score must be an integer between 1 and 5.");
            return;
        }

        TextInputDialog commentDialog = new TextInputDialog("");
        commentDialog.setTitle("Seller Feedback");
        commentDialog.setHeaderText("Leave an optional comment for the seller");
        commentDialog.setContentText("Comment:");

        Optional<String> commentResult = commentDialog.showAndWait();
        String comment = commentResult.orElse("").trim();

        String jsonBody = String.format(
                "{\"advertisementId\":%s,\"score\":%d,\"comment\":\"%s\"}",
                currentAd.getId().trim(), score, comment
        );

        String token = NetworkClient.authToken != null ? NetworkClient.authToken : "";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/ratings"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.statusCode() == 200 || response.statusCode() == 201) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Rating submitted successfully!");
                        loadSellerAverageRating(currentAd.getSellerId());
                        if (!showingProductComments) {
                            switchTabToSellerRatings();
                        }
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Failure", "Could not submit rating: " + response.body());
                    }
                }));
    }

    @FXML
    public void handleChatWithSeller() {
        if (currentAd == null) return;
        Long adId = Long.parseLong(currentAd.getId().trim());

        Thread networkThread = new Thread(() -> {
            try {
                String response = NetworkClient.createConversation(adId);
                if (response != null && response.startsWith("ERROR")) {
                    String errorMsg = response.split("\\|").length > 1 ? response.split("\\|")[1] : "Unknown error";
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Chat Error", errorMsg));
                    return;
                }

                if (response != null && !response.isBlank()) {
                    JSONObject obj = new JSONObject(response);
                    ConversationDto conv = new ConversationDto();
                    conv.setId(obj.getLong("id"));
                    conv.setAdvertisementId(adId);
                    conv.setAdvertisementTitle(currentAd.getTitle());
                    conv.setSellerId(currentAd.getSellerId());
                    conv.setSellerUsername(currentAd.getSellerName());

                    Platform.runLater(() -> NavigationUtils.openChatBox(conv));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Application Error", "Unexpected connection error."));
            }
        });
        networkThread.setDaemon(true);
        networkThread.start();
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

    private void loadProductComments(Long adId) {
        Thread thread = new Thread(() -> {
            String response = NetworkClient.getAdComments(adId);
            Platform.runLater(() -> {
                commentsListView.getItems().clear();
                if (response == null || response.startsWith("ERROR")) return;
                try {
                    JSONArray arr = new JSONArray(response);
                    if (arr.length() == 0) {
                        commentsListView.getItems().add("No public comments yet. Be the first to ask!");
                        return;
                    }
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        commentsListView.getItems().add(obj.getString("username") + ": " + obj.getString("content"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void handleSendProductComment() {
        if (currentAd == null || newCommentField.getText().trim().isBlank()) return;
        Long adId = Long.parseLong(currentAd.getId().trim());
        String content = newCommentField.getText().trim();

        Thread thread = new Thread(() -> {
            String response = NetworkClient.addAdComment(adId, content);
            Platform.runLater(() -> {
                if (response != null && !response.startsWith("ERROR")) {
                    newCommentField.clear();
                    loadProductComments(adId);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Could not post comment.");
                }
            });
        });
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void switchTabToProductComments() {
        showingProductComments = true;
        btnTabProductComments.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-weight: bold;");
        btnTabSellerRatings.setStyle("-fx-background-color: #bdc3c7; -fx-text-fill: #333; -fx-font-weight: bold;");
        if (currentAd != null) {
            loadProductComments(Long.parseLong(currentAd.getId().trim()));
        }
    }

    @FXML
    public void switchTabToSellerRatings() {
        showingProductComments = false;
        btnTabSellerRatings.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-weight: bold;");
        btnTabProductComments.setStyle("-fx-background-color: #bdc3c7; -fx-text-fill: #333; -fx-font-weight: bold;");
        if (currentAd != null) {
            loadSellerComments(currentAd.getSellerId());
        }
    }
}