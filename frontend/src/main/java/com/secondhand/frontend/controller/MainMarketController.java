package com.secondhand.frontend.controller;

import com.secondhand.frontend.model.AdvertisementDto;
import com.secondhand.frontend.network.NetworkClient;
import com.secondhand.frontend.util.NavigationUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainMarketController {

    @FXML private TextField searchField;
    @FXML private ListView<AdvertisementDto> adListView;
    @FXML private Button btnAdminPanel;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String BASE_URL = "http://localhost:8080/api/advertisements";

    @FXML
    public void initialize() {
        adListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(AdvertisementDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%s - $%.2f [By: %s]", item.getTitle(), item.getPrice(), item.getSellerName()));
                }
            }
        });

        Platform.runLater(this::loadActiveAdvertisements);

        // در انتهای متد initialize کلاس MainMarketController
        configureNavigationBasedOnRole(NetworkClient.userRole);
    }

    private void loadActiveAdvertisements() {
        adListView.getItems().clear();

        // 🟢 ارسال هدر Authorization حاوی توکن بایرِر برای گرفتن آگهی‌های فعال بدون مسدودی توسط سرور
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
                            AdvertisementDto dto = new AdvertisementDto();
                            dto.setId(obj.getLong("id"));
                            dto.setTitle(obj.getString("title"));
                            dto.setPrice(obj.getDouble("price"));
                            dto.setSellerName(obj.optString("sellerName", "Unknown"));

                            Platform.runLater(() -> adListView.getItems().add(dto));
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to parse active advertisements. Response body was: " + responseBody);
                        e.printStackTrace();
                    }
                });
    }

    @FXML
    public void goToCreatAd() {
        NavigationUtils.navigateTo(searchField, "/com/secondhand/frontend/view/create_ad.fxml", "Post a New Advertisement");
    }

    @FXML
    public void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isBlank()) {
            loadActiveAdvertisements();
            return;
        }
        System.out.println("Searching for: " + keyword);
    }

    @FXML
    public void handleLogout() {
        NetworkClient.authToken = null;
        NavigationUtils.navigateTo(searchField, "/com/secondhand/frontend/view/login.fxml", "Login");
    }

    public void configureNavigationBasedOnRole(String userRole) {
        if (btnAdminPanel != null) {
            if ("ADMIN".equals(userRole)) {
                btnAdminPanel.setVisible(true);
                btnAdminPanel.setDisable(false);
            } else {
                btnAdminPanel.setVisible(false);
                btnAdminPanel.setDisable(true);
            }
        }
    }

    @FXML
    private void handleNavigateToAdmin() {
        NavigationUtils.navigateTo(btnAdminPanel, "/com/secondhand/frontend/view/admin_panel.fxml", "Admin Dashboard");
    }
}