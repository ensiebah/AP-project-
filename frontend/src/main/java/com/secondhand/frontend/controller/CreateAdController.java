package com.secondhand.frontend.controller;

import com.secondhand.frontend.network.NetworkClient;
import com.secondhand.frontend.util.NavigationUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CreateAdController {

    @FXML private TextField titleField;
    @FXML private ComboBox<IdNamePair> categoryComboBox;
    @FXML private TextField priceField;
    @FXML private ComboBox<IdNamePair> cityComboBox;
    @FXML private TextArea descriptionArea;
    @FXML private Label errorLabel;
    @FXML private ImageView imagePreview; // New UI component

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private File selectedImageFile = null; // Holds the user's selected file in memory

    @FXML
    public void initialize() {
        setupComboBoxConverters();

        Platform.runLater(() -> {
            fetchDropdownData("/api/lookup/cities", cityComboBox);
            fetchDropdownData("/api/lookup/categories", categoryComboBox);
        });
    }

    /**
     * 📷 Action triggered by "Choose Image" button. Opens native system FileChooser.
     */
    @FXML
    public void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Image");

        // Filter out non-image extensions
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Stage stage = (Stage) titleField.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            this.selectedImageFile = file;
            Image img = new Image(file.toURI().toString());
            imagePreview.setImage(img); // Show preview immediately inside the form
        }
    }

    @FXML
    public void handleSaveAdvertisement() {
        String title = titleField.getText().trim();
        IdNamePair selectedCategory = categoryComboBox.getValue();
        IdNamePair selectedCity = cityComboBox.getValue();
        String priceText = priceField.getText().trim();
        String description = descriptionArea.getText().trim();

        if (title.isBlank() || selectedCategory == null || selectedCity == null || priceText.isBlank() || description.isBlank()) {
            errorLabel.setStyle("-fx-text-fill: red;");
            errorLabel.setText("Please fill in all fields and select options from menus.");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            errorLabel.setStyle("-fx-text-fill: red;");
            errorLabel.setText("Price must be a valid number.");
            return;
        }

        long categoryId = selectedCategory.getId();
        long cityId = selectedCity.getId();

        // 🟢 Logic for Default vs User-selected Image String URL Injection
        String finalImageUrl;
        if (selectedImageFile != null) {
            finalImageUrl = selectedImageFile.toURI().toString(); // Local file path URI
        } else {
            // Path inside target internal project resources
            finalImageUrl = getClass().getResource("/com/secondhand/frontend/images/default-ad.png") != null ?
                    getClass().getResource("/com/secondhand/frontend/images/default-ad.png").toExternalForm() :
                    "https://picsum.photos/400/200"; // Fallback placeholder if file is missing from target resources
        }

        // Combine user text description and image URL together so the double-click model parses it gracefully
        String integratedDescription = description + " [IMG_URL:" + finalImageUrl + "]";

        String jsonRequest = String.format(
                java.util.Locale.US,
                "{\"title\":\"%s\",\"description\":\"%s\",\"price\":%.2f,\"categoryId\":%d,\"cityId\":%d}",
                title, integratedDescription, price, categoryId, cityId
        );

        String response = NetworkClient.sendPostRequest("/advertisements/create", jsonRequest);

        if (response != null && !response.startsWith("ERROR")) {
            errorLabel.setStyle("-fx-text-fill: #27ae60;");
            errorLabel.setText("Advertisement published successfully! Awaiting admin approval.");

            titleField.clear();
            priceField.clear();
            descriptionArea.clear();
            categoryComboBox.setValue(null);
            cityComboBox.setValue(null);
            imagePreview.setImage(null);
            selectedImageFile = null;
        } else {
            errorLabel.setStyle("-fx-text-fill: red;");
            errorLabel.setText("Failed to publish advertisement. Server returned an error.");
        }
    }

    @FXML
    public void goBackToMarket() {
        NavigationUtils.navigateTo(titleField, "/com/secondhand/frontend/view/main_market.fxml", "SecondHand Market");
    }

    private void fetchDropdownData(String endpoint, ComboBox<IdNamePair> comboBox) {
        String token = NetworkClient.authToken != null ? NetworkClient.authToken : "";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080" + endpoint))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(responseBody -> {
                    try {
                        JSONArray array = new JSONArray(responseBody);
                        Platform.runLater(() -> {
                            comboBox.getItems().clear();
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                comboBox.getItems().add(new IdNamePair(obj.getLong("id"), obj.getString("name")));
                            }
                        });
                    } catch (Exception e) {
                        System.err.println("Failed to parse dynamic dropdown data from: " + endpoint);
                    }
                });
    }

    private void setupComboBoxConverters() {
        StringConverter<IdNamePair> converter = new StringConverter<>() {
            @Override
            public String toString(IdNamePair object) {
                return object == null ? "" : object.getName();
            }

            @Override
            public IdNamePair fromString(String string) {
                return null;
            }
        };
        categoryComboBox.setConverter(converter);
        cityComboBox.setConverter(converter);
    }

    public static class IdNamePair {
        private final long id;
        private final String name;

        public IdNamePair(long id, String name) {
            this.id = id;
            this.name = name;
        }

        public long getId() { return id; }
        public String getName() { return name; }
    }
}