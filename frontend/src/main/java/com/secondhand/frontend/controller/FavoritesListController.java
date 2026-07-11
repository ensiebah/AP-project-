package com.secondhand.frontend.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import com.secondhand.frontend.model.AdItem;
import com.secondhand.frontend.network.NetworkClient;
import com.secondhand.frontend.util.NavigationUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class FavoritesListController {

    @FXML private ListView<AdItem> favoritesListView;

    @FXML
    public void initialize() {
        // تغییر شیوه نمایش به حالت متنی مرتب
        favoritesListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(AdItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTitle() + "  |  Price: $" + item.getPrice() + "  |  City: " + item.getCity());
                }
            }
        });

        // دبل کلیک روی هر آگهی، صفحه جزئیات آن را باز می‌کند
        favoritesListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                AdItem selectedAd = favoritesListView.getSelectionModel().getSelectedItem();
                if (selectedAd != null) {
                    openAdDetails(selectedAd);
                }
            }
        });

        loadFavorites();
    }

    private void loadFavorites() {
        Thread thread = new Thread(() -> {
            String response = NetworkClient.getMyFavorites();
            if (response == null || response.startsWith("ERROR")) return;

            try {
                JSONArray arr = new JSONArray(response);
                Platform.runLater(() -> favoritesListView.getItems().clear());

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject favObj = arr.getJSONObject(i);
                    Long adId = favObj.getLong("advertisementId");

                    String adRaw = NetworkClient.getAdDetailsRaw(adId);
                    if (adRaw != null && !adRaw.startsWith("ERROR")) {
                        JSONObject adObj = new JSONObject(adRaw);

                        // استخراج داده‌ها برای فرستادن به Constructor
                        String id = String.valueOf(adObj.getLong("id"));
                        String title = adObj.getString("title");
                        String description = adObj.optString("description", "");
                        String price = String.valueOf(adObj.getDouble("price"));

                        String city = "Unknown";
                        if (adObj.has("city") && !adObj.isNull("city")) {
                            city = adObj.getJSONObject("city").optString("cityName", "Unknown");
                        }

                        String category = "Unknown";
                        if (adObj.has("category") && !adObj.isNull("category")) {
                            category = adObj.getJSONObject("category").optString("categoryName", "Unknown");
                        }

                        Long sellerId = null;
                        String sellerName = "Anonymous";
                        if (adObj.has("seller") && !adObj.isNull("seller")) {
                            JSONObject seller = adObj.getJSONObject("seller");
                            sellerId = seller.getLong("id");
                            sellerName = seller.getString("userName");
                        }

                        // 👈 اصلاح اصلی: ساخت شیء AdItem مستقیماً از طریق Constructor به دلیل final بودن فیلدها
                        AdItem item = new AdItem(id, title, description, price, city, category, sellerId, sellerName);

                        Platform.runLater(() -> favoritesListView.getItems().add(item));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void openAdDetails(AdItem ad) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/secondhand/frontend/view/ad_details.fxml"));
            Parent root = loader.load();

            AdDetailsController controller = loader.getController();
            controller.setAdData(ad);

            Stage stage = (Stage) favoritesListView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Ad Details - " + ad.getTitle());
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleBackToMarket() {
        NavigationUtils.navigateTo(favoritesListView, "/com/secondhand/frontend/view/main_market.fxml", "SecondHand Market");
    }
}