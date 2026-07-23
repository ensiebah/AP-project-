package com.secondhand.frontend.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import com.secondhand.frontend.model.AdItem;
import com.secondhand.frontend.network.NetworkClient;
import com.secondhand.frontend.util.NavigationUtils;
import com.secondhand.frontend.util.UiTheme;
import com.secondhand.frontend.util.UiMotion;
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
                    setGraphic(null);
                } else {
                    Label thumbnail = new Label("♡");
                    thumbnail.getStyleClass().add("ad-thumbnail");
                    Label title = new Label(item.getTitle());
                    title.getStyleClass().add("ad-title");
                    Label meta = new Label(item.getCategory() + "   •   " + item.getCity());
                    meta.getStyleClass().add("ad-meta");
                    VBox details = new VBox(5, title, meta);
                    HBox.setHgrow(details, Priority.ALWAYS);
                    Label price = new Label("$" + item.getPrice());
                    price.getStyleClass().add("ad-price");
                    HBox card = new HBox(14, thumbnail, details, price);
                    card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    card.getStyleClass().add("ad-card");
                    UiMotion.installCardMotion(card);
                    setText(null);
                    setGraphic(card);
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

                        // getAdDetailsRaw returns AdvertisementDto, whose fields are
                        // flat (cityName/categoryName), not nested city/category objects.
                        String city = adObj.optString("cityName", "Unknown");
                        String category = adObj.optString("categoryName", "Unknown");
                        Long sellerId = adObj.has("sellerId") && !adObj.isNull("sellerId")
                                ? adObj.getLong("sellerId") : null;
                        String sellerName = adObj.optString("sellerName", "Anonymous");

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
            UiTheme.decorate(root);

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