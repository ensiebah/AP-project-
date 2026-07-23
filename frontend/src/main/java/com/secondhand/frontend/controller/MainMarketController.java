package com.secondhand.frontend.controller;

import com.secondhand.frontend.model.AdItem;
import com.secondhand.frontend.model.AdvertisementDto;
import com.secondhand.frontend.network.NetworkClient;
import com.secondhand.frontend.util.NavigationUtils;
import com.secondhand.frontend.util.UiTheme;
import com.secondhand.frontend.util.UiMotion;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class MainMarketController {

    @FXML private TextField searchField;
    @FXML private TextField minPriceField;
    @FXML private TextField maxPriceField;
    @FXML private ComboBox<IdNamePair> parentCategoryComboBox;
    @FXML private ComboBox<IdNamePair> categoryComboBox;
    @FXML private ComboBox<IdNamePair> cityComboBox;
    @FXML private ListView<AdvertisementDto> adListView;
    @FXML private Button btnAdminPanel;
    @FXML private Button profileButton;
    @FXML private MenuButton sortMenuButton;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private static final String BASE_URL = "http://localhost:8080/api/advertisements";
    private String currentSortBy = "date";
    private String currentOrder = "desc";
    private long searchRequestVersion;

    @FXML
    public void initialize() {
        if (btnAdminPanel != null) {
            btnAdminPanel.setVisible(false);
            btnAdminPanel.setDisable(true);
        }
        if (profileButton != null) {
            profileButton.setText("👤 " + NetworkClient.currentUsername);
        }

        adListView.setCellFactory(param -> new ListCell<>() {
            {
                // ListView-level clicks can be swallowed by labels/HBoxes inside
                // a custom cell. Handling the double-click on the cell itself
                // makes opening ad details reliable.
                setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && getItem() != null) {
                        openAdDetailsPage(getItem());
                        event.consume();
                    }
                });
            }

            @Override
            protected void updateItem(AdvertisementDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label thumbnail = new Label("◈");
                thumbnail.getStyleClass().add("ad-thumbnail");

                Label title = new Label(item.getTitle());
                title.getStyleClass().add("ad-title");
                Label category = new Label("⌂ " + safeText(item.getCategoryName())
                        + "   •   ◉ " + safeText(item.getCityName()));
                category.getStyleClass().add("ad-meta");
                Label hint = new Label("Double-click to view details");
                hint.getStyleClass().add("ad-meta");
                VBox details = new VBox(5, title, category, hint);
                HBox.setHgrow(details, Priority.ALWAYS);

                Label price = new Label(String.format("$%.2f", item.getPrice()));
                price.getStyleClass().add("ad-price");
                VBox priceBox = new VBox(7, price, new Label("Active ad"));
                priceBox.setStyle("-fx-alignment: CENTER_RIGHT;");
                ((Label) priceBox.getChildren().get(1)).getStyleClass().add("ad-meta");

                Region spacer = new Region();
                spacer.setMinWidth(8);
                HBox card = new HBox(14, thumbnail, details, spacer, priceBox);
                card.getStyleClass().add("ad-card");
                card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                UiMotion.installCardMotion(card);
                setText(null);
                setGraphic(card);
            }
        });

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

        setupSortMenuOptions();
        Platform.runLater(() -> {
            loadActiveAdvertisements();
            // Root endpoint = broad categories only.
            fetchDropdownData("/api/lookup/categories", parentCategoryComboBox);
            fetchDropdownData("/api/lookup/cities", cityComboBox);
        });

        configureNavigationBasedOnRole(NetworkClient.userRole);
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "Not specified" : value;
    }

    private void setupSortMenuOptions() {
        if (sortMenuButton == null) {
            return;
        }

        sortMenuButton.getItems().clear();
        MenuItem dateItem = new MenuItem("📅 Date (Newest)");
        dateItem.setOnAction(e -> applyNewSorting("date", "desc", "Sort by: Date (Newest)"));
        MenuItem priceLowItem = new MenuItem("💰 Price: Low to High");
        priceLowItem.setOnAction(e -> applyNewSorting("price", "asc", "Sort by: Price: Low to High"));
        MenuItem priceHighItem = new MenuItem("📈 Price: High to Low");
        priceHighItem.setOnAction(e -> applyNewSorting("price", "desc", "Sort by: Price: High to Low"));
        MenuItem ratingItem = new MenuItem("⭐ Seller Rating");
        ratingItem.setOnAction(e -> applyNewSorting("rating", "desc", "Sort by: Seller Rating"));

        sortMenuButton.getItems().addAll(dateItem, priceLowItem, priceHighItem, ratingItem);
        sortMenuButton.setText("Sort by: Date (Newest)");
    }

    private void applyNewSorting(String sortBy, String order, String buttonText) {
        currentSortBy = sortBy;
        currentOrder = order;
        sortMenuButton.setText(buttonText);

        if (hasActiveFilters()) {
            handleSearch();
        } else {
            loadActiveAdvertisements();
        }
    }

    private boolean hasActiveFilters() {
        return !searchField.getText().isBlank()
                || parentCategoryComboBox.getValue() != null
                || categoryComboBox.getValue() != null
                || cityComboBox.getValue() != null
                || !minPriceField.getText().isBlank()
                || !maxPriceField.getText().isBlank();
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
        String finalUrl = BASE_URL + "/active?sortBy=" + currentSortBy + "&order=" + currentOrder;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(finalUrl))
                .header("Authorization", "Bearer " + NetworkClient.authToken)
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(responseBody -> {
                    try {
                        JSONArray jsonArray = new JSONArray(responseBody);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            AdvertisementDto dto = parseJsonToDto(jsonArray.getJSONObject(i));
                            if (!"SOLD".equalsIgnoreCase(dto.getStatus())) {
                                Platform.runLater(() -> adListView.getItems().add(dto));
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to load active advertisements.");
                    }
                });
    }

    @FXML
    public void quickFilterDigital() {
        selectQuickCategory("Digital");
    }

    @FXML
    public void quickFilterHome() {
        selectQuickCategory("Home & Kitchen");
    }

    @FXML
    public void quickFilterVehicles() {
        selectQuickCategory("Vehicles");
    }

    @FXML
    public void quickFilterEntertainment() {
        selectQuickCategory("Entertainment");
    }

    private void selectQuickCategory(String categoryName) {
        IdNamePair category = parentCategoryComboBox.getItems().stream()
                .filter(item -> categoryName.equals(item.getName()))
                .findFirst()
                .orElse(null);
        if (category != null) {
            parentCategoryComboBox.setValue(category);
            handleSearch();
        }
    }

    @FXML
    public void handleSearch() {
        String query = searchField.getText().trim();
        // A selected subcategory has priority. With only a root selected, the
        // backend returns ads belonging to every direct child of that root.
        IdNamePair selectedCategory = categoryComboBox.getValue() != null
                ? categoryComboBox.getValue() : parentCategoryComboBox.getValue();
        Long categoryId = selectedCategory == null ? null : selectedCategory.getId();
        IdNamePair selectedCity = cityComboBox.getValue();
        Long cityId = selectedCity == null ? null : selectedCity.getId();

        Double minPrice = parsePrice(minPriceField.getText());
        Double maxPrice = parsePrice(maxPriceField.getText());
        if ((!minPriceField.getText().isBlank() && minPrice == null)
                || (!maxPriceField.getText().isBlank() && maxPrice == null)) {
            adListView.setPlaceholder(new Label("Please enter valid price values."));
            return;
        }

        // NetworkClient uses a blocking HTTP call. Running it in a daemon
        // thread keeps scrolling, hover effects and buttons responsive.
        long requestVersion = ++searchRequestVersion;
        adListView.getItems().clear();
        adListView.setPlaceholder(new Label("Searching advertisements…"));

        Thread searchThread = new Thread(() -> {
            List<AdvertisementDto> results = new ArrayList<>();
            String response = NetworkClient.searchAdvertisement(
                    query, categoryId, cityId, minPrice, maxPrice, currentSortBy, currentOrder
            );

            if (response != null && !response.startsWith("ERROR")) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        AdvertisementDto dto = parseJsonToDto(jsonArray.getJSONObject(i));
                        if (!"SOLD".equalsIgnoreCase(dto.getStatus())) {
                            results.add(dto);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing search response.");
                }
            }

            Platform.runLater(() -> {
                // Ignore a slow, older search if the user already searched again.
                if (requestVersion != searchRequestVersion) {
                    return;
                }
                adListView.getItems().setAll(results);
                adListView.setPlaceholder(new Label(
                        results.isEmpty() ? "No advertisements found for these filters." : ""
                ));
            });
        }, "market-search-thread");
        searchThread.setDaemon(true);
        searchThread.start();
    }

    private Double parsePrice(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @FXML
    public void handleResetFilters() {
        searchField.clear();
        minPriceField.clear();
        maxPriceField.clear();
        parentCategoryComboBox.setValue(null);
        categoryComboBox.getItems().clear();
        categoryComboBox.setValue(null);
        categoryComboBox.setDisable(true);
        cityComboBox.setValue(null);
        currentSortBy = "date";
        currentOrder = "desc";
        if (sortMenuButton != null) {
            sortMenuButton.setText("Sort by: Date (Newest)");
        }
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
        dto.setCategoryId(obj.has("categoryId") ? obj.optLong("categoryId") : null);
        dto.setCityId(obj.has("cityId") ? obj.optLong("cityId") : null);
        dto.setCityName(obj.optString("cityName", "Unknown"));
        dto.setCategoryName(obj.optString("categoryName", "Unknown"));
        dto.setStatus(obj.optString("status", "ACTIVE"));
        dto.setImages(parseImagePaths(obj));
        return dto;
    }

    private List<String> parseImagePaths(JSONObject obj) {
        List<String> paths = new ArrayList<>();
        if (obj.has("images") && !obj.isNull("images")) {
            JSONArray images = obj.optJSONArray("images");
            if (images != null) {
                for (int i = 0; i < images.length(); i++) {
                    String path = images.optString(i, "");
                    if (!path.isBlank()) {
                        paths.add(path);
                    }
                }
            }
        }
        return paths;
    }

    private void openAdDetailsPage(AdvertisementDto dto) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/secondhand/frontend/view/ad_details.fxml")
            );
            javafx.scene.Parent root = loader.load();
            UiTheme.decorate(root);

            AdItem adItem = new AdItem(
                    String.valueOf(dto.getId()), dto.getTitle(), dto.getDescription(),
                    String.valueOf(dto.getPrice()), dto.getCityName(), dto.getCategoryName(),
                    dto.getSellerId(), dto.getSellerName(), dto.getImages()
            );

            AdDetailsController detailsController = loader.getController();
            detailsController.setAdData(adItem);

            javafx.stage.Stage stage = (javafx.stage.Stage) adListView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Ad Details - " + dto.getTitle());
            stage.show();
        } catch (Exception e) {
            // Show a visible error instead of silently failing after a double-click.
            System.err.println("Error opening Ad Details page!");
            e.printStackTrace();
            new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR,
                    "Could not open advertisement details: " + e.getMessage()
            ).show();
        }
    }

    @FXML
    public void switchToEnglish() {
        UiTheme.setPersian(false);
        UiTheme.applyLanguage((javafx.scene.Parent) searchField.getScene().getRoot());
    }

    @FXML
    public void switchToPersian() {
        UiTheme.setPersian(true);
        UiTheme.applyLanguage((javafx.scene.Parent) searchField.getScene().getRoot());
    }

    @FXML
    public void goToDashboard() {
        NavigationUtils.navigateTo(searchField, "/com/secondhand/frontend/view/user_dashboard.fxml", "My Dashboard");
    }

    @FXML
    public void goToMyAdvertisements() {
        NavigationUtils.navigateTo(searchField, "/com/secondhand/frontend/view/my_advertisements.fxml", "My Advertisements Dashboard");
    }

    @FXML
    public void goToCreatAd() {
        NavigationUtils.navigateTo(searchField, "/com/secondhand/frontend/view/create_ad.fxml", "Post a New Advertisement");
    }

    @FXML
    public void handleLogout() {
        NetworkClient.authToken = null;
        NavigationUtils.navigateTo(searchField, "/com/secondhand/frontend/view/login.fxml", "Login");
    }

    @FXML
    private void handleNavigateToAdmin() {
        NavigationUtils.navigateTo(btnAdminPanel, "/com/secondhand/frontend/view/admin_panel.fxml", "Admin Dashboard");
    }

    public void configureNavigationBasedOnRole(String userRole) {
        if (btnAdminPanel != null) {
            boolean isAdmin = "ADMIN".equals(userRole);
            btnAdminPanel.setVisible(isAdmin);
            btnAdminPanel.setDisable(!isAdmin);
        }
    }

    @FXML
    public void goToInbox() {
        NavigationUtils.navigateTo(searchField, "/com/secondhand/frontend/view/inbox.fxml", "My Inbox");
    }

    @FXML
    public void goToFavorites() {
        NavigationUtils.navigateTo(searchField, "/com/secondhand/frontend/view/favorites_list.fxml", "My Favorites");
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
}
