package com.secondhand.frontend.controller;

import com.secondhand.frontend.model.AdvertisementDto;
import com.secondhand.frontend.model.UserDto;
import com.secondhand.frontend.network.NetworkClient;
import com.secondhand.frontend.util.NavigationUtils;
import com.secondhand.frontend.util.UiMotion;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Professional administrator workspace with an overview, status-based ad
 * moderation, a read-only category tree and one searchable user directory.
 */
public class AdminPanelController {

    @FXML private StackPane contentStack;
    @FXML private VBox overviewPane;
    @FXML private VBox advertisementsPane;
    @FXML private VBox categoriesPane;
    @FXML private VBox usersPane;

    @FXML private Button overviewNavButton;
    @FXML private Button categoriesNavButton;
    @FXML private Button usersNavButton;
    @FXML private MenuButton advertisementsMenuButton;

    @FXML private Label totalAdsLabel;
    @FXML private Label pendingAdsLabel;
    @FXML private Label activeAdsLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label blockedUsersLabel;
    @FXML private Label totalCitiesLabel;
    @FXML private Label totalCategoriesLabel;
    @FXML private Label rejectedAdsLabel;
    @FXML private Label soldAdsLabel;

    @FXML private Label advertisementsTitleLabel;
    @FXML private TableView<AdvertisementDto> advertisementsTable;
    @FXML private TableColumn<AdvertisementDto, Long> adIdColumn;
    @FXML private TableColumn<AdvertisementDto, String> adTitleColumn;
    @FXML private TableColumn<AdvertisementDto, String> adCategoryColumn;
    @FXML private TableColumn<AdvertisementDto, String> adSellerColumn;
    @FXML private TableColumn<AdvertisementDto, String> adStatusColumn;
    @FXML private TableColumn<AdvertisementDto, Double> adPriceColumn;
    @FXML private Button approveAdButton;
    @FXML private Button rejectAdButton;

    @FXML private TreeView<String> categoriesTreeView;
    @FXML private Label categoryTreeSummaryLabel;

    @FXML private TextField userSearchField;
    @FXML private Button allUsersButton;
    @FXML private Button activeUsersButton;
    @FXML private Button blockedUsersButton;
    @FXML private Label usersListSummaryLabel;
    @FXML private ListView<UserDto> usersListView;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObservableList<AdvertisementDto> advertisements = FXCollections.observableArrayList();
    private final ObservableList<UserDto> allUsers = FXCollections.observableArrayList();
    private String currentUserFilter = "ALL";
    private String currentAdStatus = "PENDING";

    @FXML
    public void initialize() {
        setupAdvertisementTable();
        setupUserDirectory();
        showOverview();
        loadOverview();
        loadAdvertisements("PENDING");
        loadCategories();
        loadUsers();
    }

    private void setupAdvertisementTable() {
        adIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        adTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        adCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        adSellerColumn.setCellValueFactory(new PropertyValueFactory<>("sellerName"));
        adStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        adPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        advertisementsTable.setItems(advertisements);
        advertisementsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldAd, selectedAd) -> {
            boolean canModerate = selectedAd != null && "PENDING".equalsIgnoreCase(selectedAd.getStatus());
            approveAdButton.setDisable(!canModerate);
            rejectAdButton.setDisable(!canModerate);
        });
        approveAdButton.setDisable(true);
        rejectAdButton.setDisable(true);
    }

    private void setupUserDirectory() {
        usersListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(UserDto user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label avatar = new Label(initials(user.getFullName(), user.getUsername()));
                avatar.getStyleClass().add("ad-thumbnail");
                Label name = new Label(displayName(user));
                name.getStyleClass().add("ad-title");
                Label metadata = new Label("@" + user.getUsername() + "  •  " + safe(user.getRole(), "USER"));
                metadata.getStyleClass().add("ad-meta");
                Label state = new Label(user.isBlocked() ? "Blocked" : "Active");
                state.getStyleClass().add(user.isBlocked() ? "status-rejected" : "status-active");
                VBox details = new VBox(4, name, metadata, state);
                HBox.setHgrow(details, Priority.ALWAYS);

                Button action = new Button(user.isBlocked() ? "Unblock" : "Block");
                action.getStyleClass().add(user.isBlocked() ? "success-button" : "danger-button");
                action.setOnAction(event -> updateUserBlockState(user, !user.isBlocked()));

                HBox card = new HBox(13, avatar, details, action);
                card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                card.getStyleClass().add("row-card");
                UiMotion.installCardMotion(card);
                setText(null);
                setGraphic(card);
            }
        });
        userSearchField.textProperty().addListener((obs, oldValue, newValue) -> refreshVisibleUsers());
    }

    /* ---------- Top navigation ---------- */

    @FXML
    public void showOverview() {
        switchPane(overviewPane);
        markNav(overviewNavButton);
    }

    @FXML
    public void showAdvertisements() {
        switchPane(advertisementsPane);
        advertisementsMenuButton.setText("Advertisements · " + displayStatus(currentAdStatus));
        markNav(null);
    }

    @FXML
    public void showCategories() {
        switchPane(categoriesPane);
        markNav(categoriesNavButton);
    }

    @FXML
    public void showUsers() {
        switchPane(usersPane);
        markNav(usersNavButton);
    }

    private void switchPane(VBox target) {
        overviewPane.setVisible(target == overviewPane);
        overviewPane.setManaged(target == overviewPane);
        advertisementsPane.setVisible(target == advertisementsPane);
        advertisementsPane.setManaged(target == advertisementsPane);
        categoriesPane.setVisible(target == categoriesPane);
        categoriesPane.setManaged(target == categoriesPane);
        usersPane.setVisible(target == usersPane);
        usersPane.setManaged(target == usersPane);
    }

    private void markNav(Button selected) {
        for (Button button : List.of(overviewNavButton, categoriesNavButton, usersNavButton)) {
            button.getStyleClass().remove("nav-selected");
        }
        if (selected != null) {
            selected.getStyleClass().add("nav-selected");
        }
    }

    /* ---------- Overview ---------- */

    private void loadOverview() {
        fetchJson("/admin/dashboard/summary", object -> Platform.runLater(() -> {
            totalAdsLabel.setText(String.valueOf(object.optLong("totalAdvertisements")));
            pendingAdsLabel.setText(String.valueOf(object.optLong("pendingAdvertisements")));
            activeAdsLabel.setText(String.valueOf(object.optLong("activeAdvertisements")));
            totalUsersLabel.setText(String.valueOf(object.optLong("totalUsers")));
            blockedUsersLabel.setText(String.valueOf(object.optLong("blockedUsers")));
            totalCitiesLabel.setText(String.valueOf(object.optLong("totalCities")));
            totalCategoriesLabel.setText(String.valueOf(object.optLong("totalCategories")));
            rejectedAdsLabel.setText(String.valueOf(object.optLong("rejectedAdvertisements")));
            soldAdsLabel.setText(String.valueOf(object.optLong("soldAdvertisements")));
        }));
    }

    /* ---------- Advertisement moderation ---------- */

    @FXML public void showAllAds() { loadAdvertisements("ALL"); }
    @FXML public void showPendingAds() { loadAdvertisements("PENDING"); }
    @FXML public void showActiveAds() { loadAdvertisements("ACTIVE"); }
    @FXML public void showRejectedAds() { loadAdvertisements("REJECTED"); }
    @FXML public void showSoldAds() { loadAdvertisements("SOLD"); }
    @FXML public void showDeletedAds() { loadAdvertisements("DELETED"); }

    private void loadAdvertisements(String status) {
        currentAdStatus = status;
        advertisementsMenuButton.setText("Advertisements · " + displayStatus(status));
        advertisementsTitleLabel.setText(displayStatus(status) + " advertisements");
        advertisements.clear();
        showAdvertisements();

        fetchArray("/admin/advertisements?status=" + status, array -> {
            List<AdvertisementDto> parsed = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                AdvertisementDto dto = new AdvertisementDto();
                dto.setId(item.getLong("id"));
                dto.setTitle(item.optString("title", "Untitled advertisement"));
                dto.setPrice(item.optDouble("price", 0));
                dto.setStatus(item.optString("status", "UNKNOWN"));
                dto.setSellerName(item.optString("sellerName", "Unknown"));
                dto.setCategoryName(item.optString("categoryName", "Not specified"));
                dto.setCityName(item.optString("cityName", ""));
                parsed.add(dto);
            }
            Platform.runLater(() -> advertisements.setAll(parsed));
        });
    }

    @FXML
    public void approveSelectedAdvertisement() {
        moderateSelectedAdvertisement("approve");
    }

    @FXML
    public void rejectSelectedAdvertisement() {
        moderateSelectedAdvertisement("reject");
    }

    private void moderateSelectedAdvertisement(String action) {
        AdvertisementDto selected = advertisementsTable.getSelectionModel().getSelectedItem();
        if (selected == null || !"PENDING".equalsIgnoreCase(selected.getStatus())) {
            return;
        }
        sendPut("/admin/advertisements/" + selected.getId() + "/" + action, () -> {
            loadOverview();
            loadAdvertisements(currentAdStatus);
        });
    }

    /* ---------- Read-only category explorer ---------- */

    private void loadCategories() {
        fetchArray("/admin/categories", array -> {
            Map<Long, TreeItem<String>> itemsById = new HashMap<>();
            Map<Long, Long> parentsById = new HashMap<>();
            int rootCount = 0;

            for (int i = 0; i < array.length(); i++) {
                JSONObject category = array.getJSONObject(i);
                long id = category.getLong("id");
                String name = category.optString("name", "Unnamed category");
                TreeItem<String> item = new TreeItem<>(name);
                itemsById.put(id, item);
                if (!category.isNull("parentId")) {
                    parentsById.put(id, category.getLong("parentId"));
                } else {
                    rootCount++;
                }
            }

            TreeItem<String> root = new TreeItem<>("All categories");
            for (Map.Entry<Long, TreeItem<String>> entry : itemsById.entrySet()) {
                Long parentId = parentsById.get(entry.getKey());
                if (parentId == null || !itemsById.containsKey(parentId)) {
                    root.getChildren().add(entry.getValue());
                } else {
                    itemsById.get(parentId).getChildren().add(entry.getValue());
                }
            }
            sortTree(root);
            int finalRootCount = rootCount;
            Platform.runLater(() -> {
                root.setExpanded(true);
                categoriesTreeView.setRoot(root);
                categoriesTreeView.setShowRoot(true);
                categoryTreeSummaryLabel.setText(array.length() + " total categories across " + finalRootCount + " main groups");
            });
        });
    }

    private void sortTree(TreeItem<String> item) {
        item.getChildren().sort(Comparator.comparing(TreeItem::getValue, String.CASE_INSENSITIVE_ORDER));
        item.getChildren().forEach(this::sortTree);
    }

    /* ---------- One searchable user directory ---------- */

    @FXML public void showAllUsers() { currentUserFilter = "ALL"; refreshVisibleUsers(); markUserFilter(allUsersButton); }
    @FXML public void showActiveUsers() { currentUserFilter = "ACTIVE"; refreshVisibleUsers(); markUserFilter(activeUsersButton); }
    @FXML public void showBlockedUsers() { currentUserFilter = "BLOCKED"; refreshVisibleUsers(); markUserFilter(blockedUsersButton); }

    private void loadUsers() {
        fetchArray("/users", array -> {
            List<UserDto> parsed = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                UserDto user = new UserDto();
                user.setId(item.getLong("id"));
                user.setFullName(item.optString("fullName", ""));
                user.setUsername(item.optString("username", "unknown"));
                user.setEmail(item.optString("email", ""));
                user.setPhone(item.optString("phone", ""));
                user.setRole(item.optString("role", "USER"));
                user.setBlocked(item.optBoolean("blocked", item.optBoolean("isBlocked", false)));
                parsed.add(user);
            }
            Platform.runLater(() -> {
                allUsers.setAll(parsed);
                refreshVisibleUsers();
            });
        });
    }

    private void refreshVisibleUsers() {
        String search = userSearchField == null ? "" : userSearchField.getText().trim().toLowerCase(Locale.ROOT);
        List<UserDto> visible = allUsers.stream()
                .filter(user -> "ALL".equals(currentUserFilter)
                        || ("ACTIVE".equals(currentUserFilter) && !user.isBlocked())
                        || ("BLOCKED".equals(currentUserFilter) && user.isBlocked()))
                .filter(user -> search.isBlank()
                        || displayName(user).toLowerCase(Locale.ROOT).contains(search)
                        || user.getUsername().toLowerCase(Locale.ROOT).contains(search))
                .collect(Collectors.toList());
        usersListView.getItems().setAll(visible);
        usersListSummaryLabel.setText(visible.size() + " user" + (visible.size() == 1 ? "" : "s") + " shown");
    }

    private void markUserFilter(Button selected) {
        for (Button button : List.of(allUsersButton, activeUsersButton, blockedUsersButton)) {
            button.getStyleClass().remove("filter-selected");
        }
        selected.getStyleClass().add("filter-selected");
    }

    private void updateUserBlockState(UserDto user, boolean block) {
        String action = block ? "block" : "unblock";
        sendPut("/users/admin/users/" + user.getId() + "/" + action, () -> {
            user.setBlocked(block);
            refreshVisibleUsers();
            loadOverview();
        });
    }

    /* ---------- HTTP helpers ---------- */

    private void fetchJson(String path, java.util.function.Consumer<JSONObject> onSuccess) {
        fetch(path, body -> onSuccess.accept(new JSONObject(body)));
    }

    private void fetchArray(String path, java.util.function.Consumer<JSONArray> onSuccess) {
        fetch(path, body -> onSuccess.accept(new JSONArray(body)));
    }

    private void fetch(String path, java.util.function.Consumer<String> onSuccess) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api" + path))
                .header("Authorization", "Bearer " + NetworkClient.authToken)
                .GET()
                .build();
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        try {
                            onSuccess.accept(response.body());
                        } catch (Exception exception) {
                            System.err.println("Could not parse admin data: " + exception.getMessage());
                        }
                    } else {
                        System.err.println("Admin API returned HTTP " + response.statusCode());
                    }
                });
    }

    private void sendPut(String path, Runnable onSuccess) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api" + path))
                .header("Authorization", "Bearer " + NetworkClient.authToken)
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        onSuccess.run();
                    } else {
                        new Alert(Alert.AlertType.ERROR, "The admin action could not be completed.").show();
                    }
                }));
    }

    @FXML
    public void handleLogout() {
        NetworkClient.authToken = null;
        NetworkClient.currentUsername = "Guest";
        NetworkClient.currentFullName = "Guest";
        NavigationUtils.navigateTo(overviewNavButton, "/com/secondhand/frontend/view/login.fxml", "Sign in");
    }

    private String displayStatus(String status) {
        return switch (status) {
            case "ALL" -> "All";
            case "PENDING" -> "Pending approval";
            case "ACTIVE" -> "Published";
            case "REJECTED" -> "Rejected";
            case "SOLD" -> "Sold";
            case "DELETED" -> "Removed";
            default -> status;
        };
    }

    private String displayName(UserDto user) {
        return safe(user.getFullName(), user.getUsername());
    }

    private String initials(String fullName, String username) {
        String source = safe(fullName, username).trim();
        if (source.isEmpty()) {
            return "U";
        }
        StringBuilder initials = new StringBuilder();
        for (String part : source.split("\\s+")) {
            if (!part.isBlank()) {
                initials.append(Character.toUpperCase(part.charAt(0)));
            }
            if (initials.length() == 2) {
                break;
            }
        }
        return initials.toString();
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
