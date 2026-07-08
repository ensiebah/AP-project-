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
import com.secondhand.frontend.dto.ConversationDto;
import org.json.JSONObject;

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

        String rawDescription = ad.getDescription() != null ? ad.getDescription() : "";
        String cleanDescription = rawDescription;
        String extractedImageUrl = null;

        // 🟢 Extract image URL from description tag safely
        if (rawDescription.contains("[IMG_URL:")) {
            int start = rawDescription.indexOf("[IMG_URL:") + 9;
            int end = rawDescription.indexOf("]", start);
            if (end > start) {
                extractedImageUrl = rawDescription.substring(start, end);
                cleanDescription = rawDescription.substring(0, rawDescription.indexOf("[IMG_URL:")).trim();
            }
        }

        descriptionArea.setText(cleanDescription);

        if (sellerLabel != null) {
            sellerLabel.setText("Seller: " + ad.getSellerName());
        }

        // 🟢 Load Image logic (User Selected vs Internal Project Default Fallback)
        try {
            if (extractedImageUrl != null && !extractedImageUrl.isBlank()) {
                adImageView.setImage(new Image(extractedImageUrl, true));
            } else if (rawDescription.contains("http")) {
                adImageView.setImage(new Image(rawDescription, true));
            } else {
                // Read directly from the compiled frontend UI resources package
                String fallbackPath = getClass().getResource("/com/secondhand/frontend/images/default-ad.png") != null ?
                        getClass().getResource("/com/secondhand/frontend/images/default-ad.png").toExternalForm() :
                        "https://picsum.photos/400/200";
                adImageView.setImage(new Image(fallbackPath, true));
            }
        } catch (Exception e) {
            System.err.println("Could not load image resource, switching to web server placeholder.");
            adImageView.setImage(new Image("https://picsum.photos/400/200", true));
        }

        loadSellerAverageRating(ad.getSellerId());
    }

    @FXML
    public void handleChatWithSeller() {
        if (currentAd == null) return;

        Long adId = 1L;
        try {
            adId = Long.parseLong(currentAd.getId().trim());
        } catch (NumberFormatException e) {
            System.err.println("Warning: Ad ID is not a valid number, using fallback ID.");
        }

        try {
            String response = NetworkClient.createConversation(adId);

            if (response != null && !response.startsWith("ERROR")) {
                JSONObject obj = new JSONObject(response);
                ConversationDto conv = new ConversationDto();

                if (obj.has("id")) {
                    conv.setId(obj.getLong("id"));
                } else if (obj.has("conversationId")) {
                    conv.setId(obj.getLong("conversationId"));
                } else {
                    conv.setId(adId);
                }

                conv.setAdvertisementId(adId);
                conv.setAdvertisementTitle(currentAd.getTitle());

                Platform.runLater(() -> NavigationUtils.openChatBox(conv));
            } else {
                ConversationDto fallbackConv = new ConversationDto();
                fallbackConv.setId(adId);
                fallbackConv.setAdvertisementId(adId);
                fallbackConv.setAdvertisementTitle(currentAd.getTitle());
                Platform.runLater(() -> NavigationUtils.openChatBox(fallbackConv));
            }
        } catch (Exception e) {
            ConversationDto fallbackConv = new ConversationDto();
            fallbackConv.setId(adId);
            fallbackConv.setAdvertisementId(adId);
            fallbackConv.setAdvertisementTitle(currentAd.getTitle());
            Platform.runLater(() -> NavigationUtils.openChatBox(fallbackConv));
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

    @FXML
    public void handleRateSeller() {
        if (currentAd == null) return;

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
                currentAd.getId(), score, comment
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