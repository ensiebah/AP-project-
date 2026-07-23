package com.secondhand.frontend.controller;

import com.secondhand.frontend.network.NetworkClient;
import com.secondhand.frontend.util.NavigationUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Controller responsible for creating new advertisements.
 * A user first selects a broad category and then a required subcategory.
 */
public class CreateAdController {

    @FXML private TextField titleField;
    @FXML private ComboBox<IdNamePair> parentCategoryComboBox;
    @FXML private ComboBox<IdNamePair> categoryComboBox;
    @FXML private TextField priceField;
    @FXML private ComboBox<IdNamePair> cityComboBox;
    @FXML private TextArea descriptionArea;
    @FXML private Label errorLabel;
    @FXML private ImageView imagePreview;
    @FXML private Button publishButton;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final List<File> selectedImageFiles = new ArrayList<>();

    @FXML
    public void initialize() {
        setupComboBoxConverters();
        categoryComboBox.setDisable(true);

        parentCategoryComboBox.valueProperty().addListener((observable, oldValue, selectedParent) -> {
            categoryComboBox.getItems().clear();
            categoryComboBox.setValue(null);
            categoryComboBox.setDisable(selectedParent == null);

            if (selectedParent != null) {
                fetchDropdownData(
                        "/api/lookup/categories/" + selectedParent.getId() + "/children",
                        categoryComboBox
                );
            }
        });

        Platform.runLater(() -> {
            fetchDropdownData("/api/lookup/cities", cityComboBox);
            // This endpoint now returns only broad/root categories.
            fetchDropdownData("/api/lookup/categories", parentCategoryComboBox);
        });
    }

    @FXML
    public void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Images");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Stage stage = (Stage) titleField.getScene().getWindow();
        List<File> files = fileChooser.showOpenMultipleDialog(stage);

        if (files != null && !files.isEmpty()) {
            for (File file : files) {
                if (!selectedImageFiles.contains(file)) {
                    selectedImageFiles.add(file);
                }
            }

            Image img = new Image(files.get(files.size() - 1).toURI().toString());
            imagePreview.setImage(img);

            errorLabel.setStyle("-fx-text-fill: #2ecc71;");
            errorLabel.setText("Total " + selectedImageFiles.size() + " images selected.");
        }
    }

    @FXML
    public void handleSaveAdvertisement() {
        String title = titleField.getText().trim();
        IdNamePair selectedSubcategory = categoryComboBox.getValue();
        IdNamePair selectedCity = cityComboBox.getValue();
        String priceText = priceField.getText().trim();
        String description = descriptionArea.getText().trim();

        // A root category is intentionally not enough for an advertisement.
        if (title.isBlank() || selectedSubcategory == null || selectedCity == null
                || priceText.isBlank() || description.isBlank()) {
            showError("Please fill in all fields and select a category and a subcategory.");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            showError("Price must be a valid number.");
            return;
        }

        long categoryId = selectedSubcategory.getId();
        long cityId = selectedCity.getId();

        StringBuilder imgUrlsBuilder = new StringBuilder();
        if (!selectedImageFiles.isEmpty()) {
            for (int i = 0; i < selectedImageFiles.size(); i++) {
                imgUrlsBuilder.append(selectedImageFiles.get(i).toURI());
                if (i < selectedImageFiles.size() - 1) {
                    imgUrlsBuilder.append(",");
                }
            }
        } else {
            imgUrlsBuilder.append(getClass().getResource("/com/secondhand/frontend/images/default-ad.png") != null
                    ? getClass().getResource("/com/secondhand/frontend/images/default-ad.png").toExternalForm()
                    : "https://picsum.photos/400/200");
        }

        String integratedDescription = description + " [IMG_URL:" + imgUrlsBuilder + "]";
        String jsonRequest = String.format(
                java.util.Locale.US,
                "{\"title\":\"%s\",\"description\":\"%s\",\"price\":%.2f,\"categoryId\":%d,\"cityId\":%d}",
                title, integratedDescription, price, categoryId, cityId
        );

        // Posting can take time because it performs an HTTP request. Keep the
        // JavaFX application thread free so the button and page stay responsive.
        setPublishing(true);
        Thread publishThread = new Thread(() -> {
            String response = NetworkClient.sendPostRequest("/advertisements/create", jsonRequest);
            Platform.runLater(() -> {
                setPublishing(false);
                if (response != null && !response.startsWith("ERROR")) {
                    handleCancel();
                    errorLabel.setStyle("-fx-text-fill: #16895d;");
                    errorLabel.setText("Advertisement published successfully! Awaiting admin approval.");
                } else {
                    showError("Failed to publish advertisement. Server returned an error.");
                }
            });
        }, "create-ad-request-thread");
        publishThread.setDaemon(true);
        publishThread.start();
    }

    private void setPublishing(boolean publishing) {
        if (publishButton != null) {
            publishButton.setDisable(publishing);
            publishButton.setText(publishing ? "Publishing…" : "Publish advertisement");
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
                .thenAccept(response -> {
                    if (response.statusCode() != 200) {
                        Platform.runLater(() -> showError(
                                "Could not load categories/cities (HTTP " + response.statusCode()
                                        + "). Make sure the backend is running."
                        ));
                        return;
                    }

                    try {
                        JSONArray array = new JSONArray(response.body());
                        Platform.runLater(() -> {
                            comboBox.getItems().clear();
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                comboBox.getItems().add(new IdNamePair(obj.getLong("id"), obj.getString("name")));
                            }

                            if (array.isEmpty() && endpoint.contains("/categories")) {
                                showError("No categories were returned by the backend. Restart the backend once.");
                            }
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> showError("Could not read category data from the backend."));
                        System.err.println("Failed to parse dynamic dropdown data from: " + endpoint);
                    }
                })
                .exceptionally(error -> {
                    Platform.runLater(() -> showError(
                            "Could not connect to backend on http://localhost:8080. Start backend first."
                    ));
                    return null;
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
        parentCategoryComboBox.setConverter(converter);
        categoryComboBox.setConverter(converter);
        cityComboBox.setConverter(converter);
    }

    private void showError(String message) {
        errorLabel.setStyle("-fx-text-fill: red;");
        errorLabel.setText(message);
    }

    public static class IdNamePair {
        private final long id;
        private final String name;

        public IdNamePair(long id, String name) {
            this.id = id;
            this.name = name;
        }

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @FXML
    public void handleCancel() {
        titleField.clear();
        priceField.clear();
        descriptionArea.clear();
        parentCategoryComboBox.getSelectionModel().clearSelection();
        categoryComboBox.getSelectionModel().clearSelection();
        categoryComboBox.getItems().clear();
        categoryComboBox.setDisable(true);
        cityComboBox.getSelectionModel().clearSelection();
        imagePreview.setImage(null);
        selectedImageFiles.clear();
        errorLabel.setText("");
    }
}
