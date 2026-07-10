package com.secondhand.frontend.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import com.secondhand.frontend.network.NetworkClient;
import com.secondhand.frontend.util.NavigationUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class FavoritesListController {

    @FXML private ListView<String> favoritesListView;

    @FXML
    public void initialize() {
        loadFavorites();
    }

    private void loadFavorites() {
        Thread thread = new Thread(() -> {
            String response = NetworkClient.getMyFavorites();
            Platform.runLater(() -> {
                favoritesListView.getItems().clear();
                if (response == null || response.startsWith("ERROR")) {
                    favoritesListView.getItems().add("Failed to load favorites.");
                    return;
                }

                try {
                    JSONArray arr = new JSONArray(response);
                    if (arr.length() == 0) {
                        favoritesListView.getItems().add("Your favorites list is empty.");
                        return;
                    }

                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        favoritesListView.getItems().add("Saved Advertisement ID: " + obj.getLong("advertisementId"));
                    }
                } catch (Exception e) {
                    favoritesListView.getItems().add("Error parsing data from server.");
                }
            });
        });
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void handleBackToMarket() {
        NavigationUtils.navigateTo(favoritesListView, "/com/secondhand/frontend/view/main_market.fxml", "SecondHand Market");
    }
}