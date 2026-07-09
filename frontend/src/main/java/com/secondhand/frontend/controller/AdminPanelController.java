package com.secondhand.frontend.controller;

import com.secondhand.frontend.model.AdvertisementDto;
import com.secondhand.frontend.model.UserDto;
import com.secondhand.frontend.network.NetworkClient;
import com.secondhand.frontend.util.NavigationUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

public class AdminPanelController {

    // --- بخش مربوط به آگهی‌ها ---
    @FXML private TableView<AdvertisementDto> pendingTable;
    @FXML private TableColumn<AdvertisementDto, Long> idColumn;
    @FXML private TableColumn<AdvertisementDto, String> titleColumn;
    @FXML private TableColumn<AdvertisementDto, Double> priceColumn;
    @FXML private TableColumn<AdvertisementDto, String> sellerColumn;

    @FXML private Button btnApprove;
    @FXML private Button btnReject;
    @FXML private Button btnDeleteAd;
    @FXML private Button btnShowPending;
    @FXML private Button btnShowApproved;
    @FXML private Button btnLogout;

    // --- 🟢 بخش کاربران (این المان‌ها جا افتاده بودند و باعث خطا می‌شدند) ---
    @FXML private TableView<UserDto> activeUsersTable;
    @FXML private TableColumn<UserDto, Long> activeUserIdColumn;
    @FXML private TableColumn<UserDto, String> activeUsernameColumn;

    @FXML private TableView<UserDto> blockedUsersTable;
    @FXML private TableColumn<UserDto, Long> blockedUserIdColumn;
    @FXML private TableColumn<UserDto, String> blockedUsernameColumn;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObservableList<AdvertisementDto> adList = FXCollections.observableArrayList();

    // 🟢 لیست‌های جا افتاده برای مدیریت ساختار داده کاربران
    private final ObservableList<UserDto> activeUsersList = FXCollections.observableArrayList();
    private final ObservableList<UserDto> blockedUsersList = FXCollections.observableArrayList();

    private final String BASE_URL = "http://localhost:8080/api/advertisements";
    private final String USERS_URL = "http://localhost:8080/api/users"; // 🟢 این آدرس تعریف نشده بود

    private boolean isApprovedMode = false;

    @FXML
    public void initialize() {
        // ۱. مقداردهی جداول آگهی‌ها
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        sellerColumn.setCellValueFactory(new PropertyValueFactory<>("sellerName"));
        pendingTable.setItems(adList);

        // ۲. 🟢 مقداردهی جداول کاربران (اضافه شد برای مپ شدن درست ستون‌ها)
        activeUserIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        activeUsernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        activeUsersTable.setItems(activeUsersList);

        blockedUserIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        blockedUsernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        blockedUsersTable.setItems(blockedUsersList);

        // ۳. دبل کلیک آگهی‌ها
        pendingTable.setRowFactory(tv -> {
            TableRow<AdvertisementDto> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    AdvertisementDto rowData = row.getItem();
                    showAdDetailsAlert(rowData);
                }
            });
            return row;
        });

        // بارگذاری اطلاعات اولیه از سرور بک‌اند به صورت موازی و امن
        Platform.runLater(() -> {
            loadPendingAdvertisements();
            loadAllUsersFromServer(); // 🟢 متد لود کردن کاربران سیستم
        });
    }

    // --- متدهای آگهی‌ها ---
    private void loadPendingAdvertisements() {
        if (NetworkClient.authToken == null) return;
        fetchAdsFromServer(BASE_URL + "/pending");
    }

    private void loadApprovedAdvertisements() {
        if (NetworkClient.authToken == null) return;
        fetchAdsFromServer(BASE_URL + "/active");
    }

    private void fetchAdsFromServer(String url) {
        adList.clear();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
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
                            dto.setDescription(obj.optString("description", "No description provided."));
                            dto.setCategoryName(obj.optString("categoryName", "N/A"));
                            dto.setCityName(obj.optString("cityName", "Unknown"));

                            Platform.runLater(() -> adList.add(dto));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    @FXML
    public void showPendingMode() {
        isApprovedMode = false;
        btnShowPending.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        btnShowApproved.setStyle("-fx-background-color: #bdc3c7; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");

        btnApprove.setVisible(true);  btnApprove.setManaged(true);
        btnReject.setVisible(true);   btnReject.setManaged(true);
        btnDeleteAd.setVisible(false); btnDeleteAd.setManaged(false);

        loadPendingAdvertisements();
    }

    @FXML
    public void showApprovedMode() {
        isApprovedMode = true;
        btnShowApproved.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        btnShowPending.setStyle("-fx-background-color: #bdc3c7; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");

        btnApprove.setVisible(false); btnApprove.setManaged(false);
        btnReject.setVisible(false);  btnReject.setManaged(false);
        btnDeleteAd.setVisible(true);  btnDeleteAd.setManaged(true);

        loadApprovedAdvertisements();
    }

    @FXML
    private void handleApprove() {
        AdvertisementDto selectedAd = pendingTable.getSelectionModel().getSelectedItem();
        if (selectedAd == null) return;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + selectedAd.getId() + "/approve"))
                .header("Authorization", "Bearer " + NetworkClient.authToken)
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        Platform.runLater(() -> adList.remove(selectedAd));
                    }
                });
    }

    @FXML
    private void handleReject() {
        AdvertisementDto selectedAd = pendingTable.getSelectionModel().getSelectedItem();
        if (selectedAd == null) return;

        String rejectUrl = BASE_URL + "/" + selectedAd.getId() + "/reject";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(rejectUrl))
                .header("Authorization", "Bearer " + NetworkClient.authToken)
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        Platform.runLater(() -> adList.remove(selectedAd));
                    }
                });
    }

    @FXML
    private void handleDeleteApprovedAd() {
        AdvertisementDto selectedAd = pendingTable.getSelectionModel().getSelectedItem();
        if (selectedAd == null) return;

        String deleteUrl = BASE_URL + "/" + selectedAd.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(deleteUrl))
                .header("Authorization", "Bearer " + NetworkClient.authToken)
                .DELETE()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200 || response.statusCode() == 204) {
                        Platform.runLater(() -> adList.remove(selectedAd));
                    }
                });
    }

    private void showAdDetailsAlert(AdvertisementDto ad) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Advertisement Full Details");
        alert.setHeaderText("Viewing Ad ID: " + ad.getId() + " [" + ad.getCityName() + "]");

        alert.setContentText(
                "Title: " + ad.getTitle() + "\n" +
                        "Category: " + ad.getCategoryName() + "\n" +
                        "Price: $" + ad.getPrice() + "\n" +
                        "Seller: " + ad.getSellerName() + "\n" +
                        "-----------------------------------------\n" +
                        "Description:\n" + ad.getDescription()
        );
        alert.showAndWait();
    }

    @FXML
    private void handleLogout() {
        NetworkClient.authToken = null;
        NetworkClient.userRole = null;

        NavigationUtils.navigateTo(
                btnLogout,
                "/com/secondhand/frontend/view/login.fxml",
                "SecondHand Market - Login"
        );
    }

    // --- 🟢 متدهای پیاده‌سازی سرویس هماهنگ مدیریت کاربران با سرور ---
    private void loadAllUsersFromServer() {
        if (NetworkClient.authToken == null) return;

        activeUsersList.clear();
        blockedUsersList.clear();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(USERS_URL))
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

                            if("ADMIN".equals(obj.optString("role"))) continue;

                            UserDto user = new UserDto();
                            user.setId(obj.getLong("id"));
                            user.setUsername(obj.getString("username"));
                            user.setBlocked(obj.getBoolean("blocked"));

                            Platform.runLater(() -> {
                                if (user.isBlocked()) {
                                    blockedUsersList.add(user);
                                } else {
                                    activeUsersList.add(user);
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    @FXML
    private void handleBlockUser() {
        UserDto selectedUser = activeUsersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) return;

        String url = USERS_URL + "/admin/users/" + selectedUser.getId() + "/block";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + NetworkClient.authToken)
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        Platform.runLater(() -> {
                            activeUsersList.remove(selectedUser);
                            selectedUser.setBlocked(true);
                            blockedUsersList.add(selectedUser);
                        });
                    }
                });
    }

    @FXML
    private void handleUnblockUser() {
        UserDto selectedUser = blockedUsersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) return;

        String url = USERS_URL + "/admin/users/" + selectedUser.getId() + "/unblock";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + NetworkClient.authToken)
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        Platform.runLater(() -> {
                            blockedUsersList.remove(selectedUser);
                            selectedUser.setBlocked(false);
                            activeUsersList.add(selectedUser);
                        });
                    }
                });
    }
}