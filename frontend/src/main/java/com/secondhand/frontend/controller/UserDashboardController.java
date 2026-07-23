package com.secondhand.frontend.controller;

import com.secondhand.frontend.network.NetworkClient;
import com.secondhand.frontend.util.NavigationUtils;
import com.secondhand.frontend.util.UiTheme;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Personal marketplace dashboard. It uses existing APIs only: my ads,
 * favorites and conversations. No additional database table is required.
 */
public class UserDashboardController {

    @FXML private Label avatarLabel;
    @FXML private Label fullNameLabel;
    @FXML private Label usernameLabel;
    @FXML private Label roleLabel;
    @FXML private Label totalAdsLabel;
    @FXML private Label activeAdsLabel;
    @FXML private Label favoritesLabel;
    @FXML private Label conversationsLabel;
    @FXML private ListView<String> activityListView;

    @FXML
    public void initialize() {
        String fullName = NetworkClient.currentFullName == null || NetworkClient.currentFullName.isBlank()
                ? NetworkClient.currentUsername : NetworkClient.currentFullName;
        fullNameLabel.setText(fullName);
        usernameLabel.setText("@" + NetworkClient.currentUsername);
        roleLabel.setText("ADMIN".equals(NetworkClient.userRole) ? "Administrator account" : "Marketplace member");
        avatarLabel.setText(makeInitials(fullName));
        activityListView.getItems().setAll("Loading your marketplace summary…");
        loadDashboardData();
    }

    private String makeInitials(String name) {
        if (name == null || name.isBlank()) {
            return "U";
        }
        String[] words = name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (String word : words) {
            if (!word.isBlank()) {
                initials.append(Character.toUpperCase(word.charAt(0)));
            }
            if (initials.length() == 2) {
                break;
            }
        }
        return initials.toString();
    }

    private void loadDashboardData() {
        Thread loader = new Thread(() -> {
            int totalAds = 0;
            int activeAds = 0;
            int favorites = 0;
            int conversations = 0;
            String newestActivity = "No marketplace activity yet.";

            try {
                String adsResponse = NetworkClient.getMyAdvertisements();
                if (isSuccessful(adsResponse)) {
                    JSONArray ads = new JSONArray(adsResponse);
                    totalAds = ads.length();
                    for (int i = 0; i < ads.length(); i++) {
                        JSONObject ad = ads.getJSONObject(i);
                        if ("ACTIVE".equalsIgnoreCase(ad.optString("status"))) {
                            activeAds++;
                        }
                    }
                    if (!ads.isEmpty()) {
                        newestActivity = "Latest ad: " + ads.getJSONObject(0).optString("title", "Untitled ad");
                    }
                }

                String favoritesResponse = NetworkClient.getMyFavorites();
                if (isSuccessful(favoritesResponse)) {
                    favorites = new JSONArray(favoritesResponse).length();
                }

                String chatsResponse = NetworkClient.getMyChats();
                if (isSuccessful(chatsResponse)) {
                    conversations = new JSONArray(chatsResponse).length();
                }
            } catch (Exception ignored) {
                newestActivity = "Your dashboard will refresh when the server is available.";
            }

            int finalTotalAds = totalAds;
            int finalActiveAds = activeAds;
            int finalFavorites = favorites;
            int finalConversations = conversations;
            String finalNewestActivity = newestActivity;
            Platform.runLater(() -> {
                totalAdsLabel.setText(String.valueOf(finalTotalAds));
                activeAdsLabel.setText(String.valueOf(finalActiveAds));
                favoritesLabel.setText(String.valueOf(finalFavorites));
                conversationsLabel.setText(String.valueOf(finalConversations));
                activityListView.getItems().setAll(
                        finalNewestActivity,
                        "You have " + finalFavorites + " saved advertisement(s).",
                        "You have " + finalConversations + " conversation(s)."
                );
            });
        });
        loader.setDaemon(true);
        loader.start();
    }

    private boolean isSuccessful(String response) {
        return response != null && !response.startsWith("ERROR");
    }

    @FXML
    public void switchToEnglish() {
        UiTheme.setPersian(false);
        UiTheme.applyLanguage((javafx.scene.Parent) fullNameLabel.getScene().getRoot());
    }

    @FXML
    public void switchToPersian() {
        UiTheme.setPersian(true);
        UiTheme.applyLanguage((javafx.scene.Parent) fullNameLabel.getScene().getRoot());
    }

    @FXML
    public void goToMarket() {
        NavigationUtils.navigateTo(fullNameLabel, "/com/secondhand/frontend/view/main_market.fxml", "SecondHand Market");
    }

    @FXML
    public void goToMyAds() {
        NavigationUtils.navigateTo(fullNameLabel, "/com/secondhand/frontend/view/my_advertisements.fxml", "My Advertisements");
    }

    @FXML
    public void goToFavorites() {
        NavigationUtils.navigateTo(fullNameLabel, "/com/secondhand/frontend/view/favorites_list.fxml", "Saved Advertisements");
    }

    @FXML
    public void goToMessages() {
        NavigationUtils.navigateTo(fullNameLabel, "/com/secondhand/frontend/view/inbox.fxml", "My Messages");
    }

    @FXML
    public void goToCreateAd() {
        NavigationUtils.navigateTo(fullNameLabel, "/com/secondhand/frontend/view/create_ad.fxml", "Post New Advertisement");
    }

    @FXML
    public void logout() {
        NetworkClient.authToken = null;
        NetworkClient.currentUsername = "Guest";
        NetworkClient.currentFullName = "Guest";
        NavigationUtils.navigateTo(fullNameLabel, "/com/secondhand/frontend/view/login.fxml", "Sign in");
    }
}
