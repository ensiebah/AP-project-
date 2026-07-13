package com.secondhand.frontend.controller;

import com.secondhand.frontend.model.AdItem;
import com.secondhand.frontend.model.AdvertisementDto;
import com.secondhand.frontend.network.NetworkClient;
import com.secondhand.frontend.util.NavigationUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Region; // 🟢 رفع خطای نبودن کلاس Region
import javafx.util.StringConverter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class MainMarketController {

    @FXML private TextField searchField;
    @FXML private TextField minPriceField;
    @FXML private TextField maxPriceField;
    @FXML private ComboBox<IdNamePair> categoryComboBox;
    @FXML private ComboBox<IdNamePair> cityComboBox;
    @FXML private ListView<AdvertisementDto> adListView;
    @FXML private Button btnAdminPanel;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String BASE_URL = "http://localhost:8080/api/advertisements";

    @FXML
    public void initialize() {
        if (btnAdminPanel != null) {
            btnAdminPanel.setVisible(false);
            btnAdminPanel.setDisable(true);
        }

        adListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(AdvertisementDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%s - $%.2f [City: %s]", item.getTitle(), item.getPrice(), item.getCityName()));
                }
            }
        });

        adListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                AdvertisementDto selectedDto = adListView.getSelectionModel().getSelectedItem();
                if (selectedDto != null) {
                    openAdDetailsPage(selectedDto);
                }
            }
        });

        setupComboBoxConverters();

        Platform.runLater(() -> {
            loadActiveAdvertisements();
            fetchDropdownData("/api/lookup/categories", categoryComboBox);
            fetchDropdownData("/api/lookup/cities", cityComboBox);
        });

        configureNavigationBasedOnRole(NetworkClient.userRole);
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
                        System.err.println("Failed to parse dropdown data from: " + endpoint);
                    }
                });
    }

    private void loadActiveAdvertisements() {
        adListView.getItems().clear();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/active"))
                .header("Authorization", "Bearer " + NetworkClient.authToken)
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(responseBody -> {
                    try {
                        JSONArray jsonArray = new JSONArray(responseBody);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            AdvertisementDto dto = parseJsonToDto(obj);
                            Platform.runLater(() -> adListView.getItems().add(dto));
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to load active advertisements.");
                    }
                });
    }

    @FXML
    public void handleSearch() {
        String query = searchField.getText().trim();

        IdNamePair selectedCategory = categoryComboBox.getValue();
        Long categoryId = selectedCategory != null ? selectedCategory.getId() : null;

        IdNamePair selectedCity = cityComboBox.getValue();
        Long cityId = selectedCity != null ? selectedCity.getId() : null;

        Double minPrice = null;
        Double maxPrice = null;

        try {
            if (!minPriceField.getText().isBlank()) minPrice = Double.parseDouble(minPriceField.getText().trim());
            if (!maxPriceField.getText().isBlank()) maxPrice = Double.parseDouble(maxPriceField.getText().trim());
        } catch (NumberFormatException e) {
            System.err.println("Invalid format for price range filters.");
        }

        String response = NetworkClient.searchAdvertisement(query, categoryId, cityId, minPrice, maxPrice);

        adListView.getItems().clear();
        if (response != null && !response.startsWith("ERROR")) {
            try {
                JSONArray jsonArray = new JSONArray(response);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    AdvertisementDto dto = parseJsonToDto(obj);
                    Platform.runLater(() -> adListView.getItems().add(dto));
                }
            } catch (Exception e) {
                System.err.println("Error parsing search response.");
            }
        }
    }

    @FXML
    public void handleResetFilters() {
        searchField.clear();
        minPriceField.clear();
        maxPriceField.clear();
        categoryComboBox.setValue(null);
        cityComboBox.setValue(null);
        loadActiveAdvertisements();
    }

    private AdvertisementDto parseJsonToDto(JSONObject obj) {
        AdvertisementDto dto = new AdvertisementDto();
        dto.setId(obj.getLong("id"));
        dto.setTitle(obj.getString("title"));
        dto.setPrice(obj.getDouble("price"));
        dto.setDescription(obj.optString("description", "No description available."));
        dto.setSellerId(obj.optLong("sellerId", 1L));
        dto.setSellerName(obj.optString("sellerName", "Unknown"));
        dto.setCityName(obj.optString("cityName", "Unknown"));
        dto.setCategoryName(obj.optString("categoryName", "Unknown"));
        return dto;
    }

    private void openAdDetailsPage(AdvertisementDto dto) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/secondhand/frontend/view/ad_details.fxml")
            );
            javafx.scene.Parent root = loader.load();

            AdItem adItem = new AdItem(
                    String.valueOf(dto.getId()),
                    dto.getTitle(),
                    dto.getDescription(),
                    String.valueOf(dto.getPrice()),
                    dto.getCityName(),
                    dto.getCategoryName(),
                    dto.getSellerId(),
                    dto.getSellerName()
            );

            AdDetailsController detailsController = loader.getController();
            detailsController.setAdData(adItem);

            javafx.stage.Stage stage = (javafx.stage.Stage) adListView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Ad Details - " + dto.getTitle());
            stage.show();
        } catch (java.io.IOException e) {
            System.err.println("Error opening Ad Details page!");
            e.printStackTrace();
        }
    }

    @FXML
    public void goToMyAdvertisements() {
        NavigationUtils.navigateTo(searchField, "/com/secondhand/frontend/view/my_advertisements.fxml", "My Advertisements Dashboard");
    }

    @FXML public void goToCreatAd() { NavigationUtils.navigateTo(searchField, "/com/secondhand/frontend/view/create_ad.fxml", "Post a New Advertisement"); }
    @FXML public void handleLogout() { NetworkClient.authToken = null; NavigationUtils.navigateTo(searchField, "/com/secondhand/frontend/view/login.fxml", "Login"); }
    @FXML private void handleNavigateToAdmin() { NavigationUtils.navigateTo(btnAdminPanel, "/com/secondhand/frontend/view/admin_panel.fxml", "Admin Dashboard"); }

    public void configureNavigationBasedOnRole(String userRole) {
        if (btnAdminPanel != null) {
            boolean isAdmin = "ADMIN".equals(userRole);
            btnAdminPanel.setVisible(isAdmin);
            btnAdminPanel.setDisable(!isAdmin);
        }
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

    @FXML
    public void goToInbox() {
        NavigationUtils.navigateTo(searchField, "/com/secondhand/frontend/view/inbox.fxml", "My Inbox");
    }

    @FXML
    public void goToFavorites() {
        NavigationUtils.navigateTo(searchField, "/com/secondhand/frontend/view/favorites_list.fxml", "My Favorites");
    }
}