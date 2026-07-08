package com.secondhand.frontend.controller;

import com.secondhand.frontend.network.NetworkClient;
import com.secondhand.frontend.util.NavigationUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.json.JSONArray;
import org.json.JSONObject;

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

    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * 🏁 This method runs automatically when the view loads.
     * It configures the drop-downs and fetches lists from the backend.
     */
    @FXML
    public void initialize() {
        setupComboBoxConverters();

        // Fetch data asynchronously from the server so the UI does not freeze
        Platform.runLater(() -> {
            fetchDropdownData("/api/lookup/cities", cityComboBox);
            fetchDropdownData("/api/lookup/categories", categoryComboBox);
        });
    }

    /**
     * 🟢 Form Submission Logic
     */
    @FXML
    public void handleSaveAdvertisement() {
        String title = titleField.getText().trim();
        IdNamePair selectedCategory = categoryComboBox.getValue();
        IdNamePair selectedCity = cityComboBox.getValue();
        String priceText = priceField.getText().trim();
        String description = descriptionArea.getText().trim();

        // 1. Validation for empty selections
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

        // 2. Extract hidden database IDs from objects
        long categoryId = selectedCategory.getId();
        long cityId = selectedCity.getId();

        // 3. Construct clean JSON payload
        String jsonRequest = String.format(
                java.util.Locale.US,
                "{\"title\":\"%s\",\"description\":\"%s\",\"price\":%.2f,\"categoryId\":%d,\"cityId\":%d}",
                title, description, price, categoryId, cityId
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
        } else {
            errorLabel.setStyle("-fx-text-fill: red;");
            errorLabel.setText("Failed to publish advertisement. Server returned an error.");
        }
    }

    @FXML
    public void goBackToMarket() {
        NavigationUtils.navigateTo(titleField, "/com/secondhand/frontend/view/main_market.fxml", "SecondHand Market");
    }

    /**
     * 🔄 Network Helper: Fetches JSON lookup data and populates specified ComboBox
     */
    private void fetchDropdownData(String endpoint, ComboBox<IdNamePair> comboBox) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080" + endpoint))
                .header("Authorization", "Bearer " + NetworkClient.authToken)
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

    /**
     * 🛠️ UX Helper: Instructs JavaFX to display ONLY the name property to users
     */
    private void setupComboBoxConverters() {
        StringConverter<IdNamePair> converter = new StringConverter<>() {
            @Override
            public String toString(IdNamePair object) {
                return object == null ? "" : object.getName();
            }

            @Override
            public IdNamePair fromString(String string) {
                return null; // Not needed for read-only selections
            }
        };
        categoryComboBox.setConverter(converter);
        cityComboBox.setConverter(converter);
    }

    /**
     * 📦 Local Helper Entity Class
     * Holds both Database Identifier and Human Readable String together.
     */
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