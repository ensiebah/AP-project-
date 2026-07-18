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

    // 🟢 اضافه شدن کنترلر دکمه کشویی مرتب‌سازی جدید
    @FXML private MenuButton sortMenuButton;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String BASE_URL = "http://localhost:8080/api/advertisements";

    // 🟢 ذخیره وضعیت‌های فعلی مرتب‌سازی (مقادیر پیش‌فرض بر اساس تاریخ ثبت نزولی)
    private String currentSortBy = "date";
    private String currentOrder = "desc";

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

        // 🟢 راه‌اندازی گزینه‌های داخل منوی مرتب‌سازی
        setupSortMenuOptions();

        Platform.runLater(() -> {
            loadActiveAdvertisements();
            fetchDropdownData("/api/lookup/categories", categoryComboBox);
            fetchDropdownData("/api/lookup/cities", cityComboBox);
        });

        configureNavigationBasedOnRole(NetworkClient.userRole);
    }

    /**
     * 🟢 متد جدید برای تنظیم منوها و رویدادهای تغییر مرتب‌سازی
     */
    private void setupSortMenuOptions() {
        if (sortMenuButton == null) return;

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
        // ست کردن متن دکمه بر روی حالت پیش‌فرض
        sortMenuButton.setText("Sort by: Date (Newest)");
    }

    /**
     * 🟢 اعمال متد تغییر نوع مرتب‌سازی و بازخوانی مجدد لیست داده‌ها
     */
    private void applyNewSorting(String sortBy, String order, String buttonText) {
        this.currentSortBy = sortBy;
        this.currentOrder = order;
        this.sortMenuButton.setText(buttonText);

        // اگر کادر سرچ یا فیلترها پر باشند از هندلر سرچ استفاده کن در غیر این صورت لیست پیش‌فرض اکتیو را لود کن
        if (!searchField.getText().isBlank() || categoryComboBox.getValue() != null ||
                cityComboBox.getValue() != null || !minPriceField.getText().isBlank() || !maxPriceField.getText().isBlank()) {
            handleSearch();
        } else {
            loadActiveAdvertisements();
        }
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
                            JSONObject obj = jsonArray.getJSONObject(i);
                            AdvertisementDto dto = parseJsonToDto(obj);

                            // 🟢 فیلتر کردن بر اساس شیء پارس شده‌ی معتبر
                            if ("SOLD".equalsIgnoreCase(dto.getStatus())) {
                                continue;
                            }

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

        String response = NetworkClient.searchAdvertisement(query, categoryId, cityId, minPrice, maxPrice, currentSortBy, currentOrder);

        adListView.getItems().clear();
        if (response != null && !response.startsWith("ERROR")) {
            try {
                JSONArray jsonArray = new JSONArray(response);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    AdvertisementDto dto = parseJsonToDto(obj);

                    // 🟢 فیلتر کردن آگهی فروخته شده در نتایج سرچ و فیلترها
                    if ("SOLD".equalsIgnoreCase(dto.getStatus())) {
                        continue;
                    }

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

        // 🟢 ریست کردن وضعیت مرتب‌سازی به حالت اولیه زمان تایید
        this.currentSortBy = "date";
        this.currentOrder = "desc";
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
        dto.setCityName(obj.optString("cityName", "Unknown"));
        dto.setCategoryName(obj.optString("categoryName", "Unknown"));

        // 🟢 اضافه کردن این خط بسیار حیاتی است: خواندن وضعیت آگهی از جی‌سون سرور
        dto.setStatus(obj.optString("status", "ACTIVE"));

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