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
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    @FXML private Label advertisementsSummaryLabel;
    @FXML private ListView<AdvertisementDto> advertisementsListView;

    @FXML private TreeView<String> categoriesTreeView;
    @FXML private Label categoryTreeSummaryLabel;
    @FXML private Label selectedCategoryInfoLabel;
    @FXML private TextField categoryNameField;
    @FXML private Button addSubcategoryButton;
    @FXML private Button renameCategoryButton;
    @FXML private Button deleteCategoryButton;

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
    private final Map<TreeItem<String>, Long> categoryItemIds = new HashMap<>();

    @FXML
    public void initialize() {
        setupAdvertisementCards();
        setupUserDirectory();
        setupCategoryExplorer();
        showOverview();
        loadOverview();
        loadAdvertisements("PENDING");
        loadCategories();
        loadUsers();
        // Preload pending cards, but land the administrator on the overview.
        showOverview();
    }

    private void setupAdvertisementCards() {
        advertisementsListView.setItems(advertisements);
        advertisementsListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(AdvertisementDto ad, boolean empty) {
                super.updateItem(ad, empty);
                if (empty || ad == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                ImageView imageView = new ImageView();
                imageView.setFitWidth(118);
                imageView.setFitHeight(92);
                imageView.setPreserveRatio(true);
                imageView.getStyleClass().add("admin-ad-image");
                if (ad.getImages() != null && !ad.getImages().isEmpty()) {
                    try {
                        imageView.setImage(new Image(
                                NetworkClient.toAbsoluteImageUrl(ad.getImages().get(0)), true
                        ));
                    } catch (Exception ignored) {
                        // The styled empty preview remains visible if an old image is unavailable.
                    }
                }

                Label title = new Label(ad.getTitle());
                title.getStyleClass().add("ad-title");
                Label meta = new Label(
                        "Seller: " + safe(ad.getSellerName(), "Unknown")
                                + "   •   " + safe(ad.getCategoryName(), "No category")
                                + "   •   " + safe(ad.getCityName(), "No city")
                );
                meta.getStyleClass().add("ad-meta");
                Label price = new Label(String.format("$%.2f", ad.getPrice()));
                price.getStyleClass().add("ad-price");
                Label status = new Label(displayStatus(ad.getStatus()));
                status.getStyleClass().add(statusStyleClass(ad.getStatus()));

                VBox information = new VBox(6, title, meta, price);
                if ("REJECTED".equalsIgnoreCase(ad.getStatus())
                        && ad.getRejectionReason() != null
                        && !ad.getRejectionReason().isBlank()) {
                    Label reason = new Label("Rejection feedback: " + ad.getRejectionReason());
                    reason.setWrapText(true);
                    reason.getStyleClass().add("rejection-reason");
                    information.getChildren().add(reason);
                }
                HBox.setHgrow(information, Priority.ALWAYS);

                VBox rightSide = new VBox(9, status, createAdActions(ad));
                rightSide.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                HBox card = new HBox(15, imageView, information, rightSide);
                card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                card.getStyleClass().add("admin-ad-card");
                UiMotion.installCardMotion(card);
                setText(null);
                setGraphic(card);
            }
        });
    }

    private HBox createAdActions(AdvertisementDto ad) {
        HBox actions = new HBox(7);
        actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        String status = ad.getStatus() == null ? "" : ad.getStatus().toUpperCase(Locale.ROOT);

        switch (status) {
            case "PENDING" -> {
                actions.getChildren().addAll(
                        actionButton("Approve", "success-button", () -> approveAdvertisement(ad)),
                        actionButton("Reject", "danger-button", () -> rejectAdvertisement(ad)),
                        actionButton("Delete", "outline-button", () -> deleteAdvertisement(ad))
                );
            }
            case "ACTIVE" -> actions.getChildren().addAll(
                    actionButton("Mark sold", "success-button", () -> markAdvertisementSold(ad)),
                    actionButton("Delete", "danger-button", () -> deleteAdvertisement(ad))
            );
            case "REJECTED" -> actions.getChildren().addAll(
                    actionButton("Approve again", "success-button", () -> approveAdvertisement(ad)),
                    actionButton("Delete", "danger-button", () -> deleteAdvertisement(ad))
            );
            case "SOLD" -> actions.getChildren().add(
                    actionButton("Delete", "danger-button", () -> deleteAdvertisement(ad))
            );
            case "DELETED" -> {
                Label archived = new Label("Archived — no restore action");
                archived.getStyleClass().add("ad-meta");
                actions.getChildren().add(archived);
            }
            default -> { }
        }
        return actions;
    }

    private Button actionButton(String text, String styleClass, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add(styleClass);
        button.setOnAction(event -> action.run());
        return button;
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

    private void setupCategoryExplorer() {
        categoriesTreeView.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, selectedItem) -> {
            Long categoryId = categoryItemIds.get(selectedItem);
            boolean hasSelection = categoryId != null;
            addSubcategoryButton.setDisable(!hasSelection);
            renameCategoryButton.setDisable(!hasSelection);
            deleteCategoryButton.setDisable(!hasSelection);

            if (hasSelection) {
                selectedCategoryInfoLabel.setText("Selected: " + selectedItem.getValue());
                categoryNameField.setText(selectedItem.getValue());
            } else {
                selectedCategoryInfoLabel.setText("Select a category to add a child, rename it or delete it.");
                categoryNameField.clear();
            }
        });
        addSubcategoryButton.setDisable(true);
        renameCategoryButton.setDisable(true);
        deleteCategoryButton.setDisable(true);
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
        advertisementsSummaryLabel.setText("Loading " + displayStatus(status).toLowerCase(Locale.ROOT) + " advertisements…");
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
                dto.setRejectionReason(item.optString("rejectionReason", ""));
                dto.setSellerName(item.optString("sellerName", "Unknown"));
                dto.setCategoryName(item.optString("categoryName", "Not specified"));
                dto.setCityName(item.optString("cityName", ""));
                List<String> imagePaths = new ArrayList<>();
                JSONArray images = item.optJSONArray("images");
                if (images != null) {
                    for (int imageIndex = 0; imageIndex < images.length(); imageIndex++) {
                        String imagePath = images.optString(imageIndex, "");
                        if (!imagePath.isBlank()) {
                            imagePaths.add(imagePath);
                        }
                    }
                }
                dto.setImages(imagePaths);
                parsed.add(dto);
            }
            Platform.runLater(() -> {
                advertisements.setAll(parsed);
                advertisementsSummaryLabel.setText(parsed.size() + " " + displayStatus(status).toLowerCase(Locale.ROOT) + " advertisement(s)");
            });
        });
    }

    private void approveAdvertisement(AdvertisementDto ad) {
        sendPut("/admin/advertisements/" + ad.getId() + "/approve", this::refreshAdvertisementData);
    }

    private void rejectAdvertisement(AdvertisementDto ad) {
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
        dialog.setTitle("Reject advertisement");
        dialog.setHeaderText("Tell the seller why this advertisement was rejected");
        dialog.setContentText("Rejection reason:");
        dialog.showAndWait().ifPresent(reason -> {
            if (reason.isBlank()) {
                showAdminWarning("A rejection reason is required.");
                return;
            }
            JSONObject body = new JSONObject().put("reason", reason.trim());
            sendJsonPut(
                    "/admin/advertisements/" + ad.getId() + "/reject",
                    body.toString(),
                    this::refreshAdvertisementData
            );
        });
    }

    private void markAdvertisementSold(AdvertisementDto ad) {
        sendPut("/admin/advertisements/" + ad.getId() + "/sold", this::refreshAdvertisementData);
    }

    private void deleteAdvertisement(AdvertisementDto ad) {
        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Move '" + ad.getTitle() + "' to Deleted advertisements?",
                javafx.scene.control.ButtonType.YES,
                javafx.scene.control.ButtonType.NO
        );
        confirmation.setHeaderText("Delete advertisement");
        confirmation.showAndWait().ifPresent(choice -> {
            if (choice == javafx.scene.control.ButtonType.YES) {
                sendDelete("/admin/advertisements/" + ad.getId(), this::refreshAdvertisementData);
            }
        });
    }

    private void refreshAdvertisementData() {
        loadOverview();
        loadAdvertisements(currentAdStatus);
    }

    /* ---------- Category management ---------- */

    private void loadCategories() {
        fetchArray("/admin/categories", array -> {
            Map<Long, TreeItem<String>> itemsById = new HashMap<>();
            Map<Long, Long> parentsById = new HashMap<>();
            Map<TreeItem<String>, Long> builtItemIds = new HashMap<>();
            int rootCount = 0;

            for (int i = 0; i < array.length(); i++) {
                JSONObject category = array.getJSONObject(i);
                long id = category.getLong("id");
                String name = category.optString("name", "Unnamed category");
                TreeItem<String> item = new TreeItem<>(name);
                itemsById.put(id, item);
                builtItemIds.put(item, id);
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
                categoryItemIds.clear();
                categoryItemIds.putAll(builtItemIds);
                root.setExpanded(true);
                categoriesTreeView.setRoot(root);
                categoriesTreeView.setShowRoot(true);
                categoriesTreeView.getSelectionModel().clearSelection();
                categoryTreeSummaryLabel.setText(
                        array.length() + " total categories across " + finalRootCount + " main groups"
                );
            });
        });
    }

    @FXML
    public void addRootCategory() {
        createCategory(null);
    }

    @FXML
    public void addSubcategory() {
        Long parentId = selectedCategoryId();
        if (parentId == null) {
            showCategoryError("Select a parent category first.");
            return;
        }
        createCategory(parentId);
    }

    private void createCategory(Long parentId) {
        String name = categoryNameField.getText().trim();
        if (name.isBlank()) {
            showCategoryError("Enter a category name first.");
            return;
        }
        JSONObject body = new JSONObject().put("name", name);
        if (parentId != null) {
            body.put("parentId", parentId);
        }
        sendCategoryRequest("POST", "/admin/categories", body.toString(), this::refreshCategoryData);
    }

    @FXML
    public void renameSelectedCategory() {
        Long categoryId = selectedCategoryId();
        String name = categoryNameField.getText().trim();
        if (categoryId == null) {
            showCategoryError("Select a category to rename.");
            return;
        }
        if (name.isBlank()) {
            showCategoryError("Enter a new category name first.");
            return;
        }
        sendCategoryRequest(
                "PUT", "/admin/categories/" + categoryId,
                new JSONObject().put("name", name).toString(), this::refreshCategoryData
        );
    }

    @FXML
    public void deleteSelectedCategory() {
        Long categoryId = selectedCategoryId();
        if (categoryId == null) {
            showCategoryError("Select a category to delete.");
            return;
        }
        String name = categoriesTreeView.getSelectionModel().getSelectedItem().getValue();
        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Delete '" + name + "'? This is allowed only when it has no subcategories and no advertisements.",
                javafx.scene.control.ButtonType.YES,
                javafx.scene.control.ButtonType.NO
        );
        confirmation.setHeaderText("Delete category");
        confirmation.showAndWait().ifPresent(choice -> {
            if (choice == javafx.scene.control.ButtonType.YES) {
                sendCategoryRequest("DELETE", "/admin/categories/" + categoryId, null, this::refreshCategoryData);
            }
        });
    }

    private Long selectedCategoryId() {
        return categoryItemIds.get(categoriesTreeView.getSelectionModel().getSelectedItem());
    }

    private void refreshCategoryData() {
        categoryNameField.clear();
        loadCategories();
        loadOverview();
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

    private void sendCategoryRequest(String method, String path, String body, Runnable onSuccess) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api" + path))
                .header("Authorization", "Bearer " + NetworkClient.authToken);
        if (body != null) {
            requestBuilder.header("Content-Type", "application/json");
        }
        requestBuilder.method(method, body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body));

        httpClient.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        onSuccess.run();
                    } else {
                        showCategoryError(response.body().isBlank()
                                ? "The category operation could not be completed."
                                : response.body());
                    }
                }));
    }

    private void showCategoryError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message);
        alert.setHeaderText("Category management");
        alert.show();
    }

    private void sendJsonPut(String path, String jsonBody, Runnable onSuccess) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api" + path))
                .header("Authorization", "Bearer " + NetworkClient.authToken)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        onSuccess.run();
                    } else {
                        showAdminWarning(response.body().isBlank()
                                ? "The moderation action could not be completed."
                                : response.body());
                    }
                }));
    }

    private void showAdminWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message);
        alert.setHeaderText("Advertisement moderation");
        alert.show();
    }

    private void sendDelete(String path, Runnable onSuccess) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api" + path))
                .header("Authorization", "Bearer " + NetworkClient.authToken)
                .DELETE()
                .build();
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        onSuccess.run();
                    } else {
                        showAdminWarning("The delete action could not be completed.");
                    }
                }));
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

    private String statusStyleClass(String status) {
        return switch (status == null ? "" : status.toUpperCase(Locale.ROOT)) {
            case "ACTIVE" -> "status-active";
            case "REJECTED", "DELETED" -> "status-rejected";
            case "SOLD" -> "status-sold";
            default -> "status-pending";
        };
    }

    private String displayStatus(String status) {
        return switch (status) {
            case "ALL" -> "All";
            case "PENDING" -> "Pending approval";
            case "ACTIVE" -> "Approved";
            case "REJECTED" -> "Rejected";
            case "SOLD" -> "Sold out";
            case "DELETED" -> "Deleted";
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
