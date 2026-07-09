package com.secondhand.frontend.controller;

import com.secondhand.frontend.model.AdvertisementDto;
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
    @FXML private Button btnLogout; // 🟢 دکمه جدید خروج

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObservableList<AdvertisementDto> adList = FXCollections.observableArrayList();
    private final String BASE_URL = "http://localhost:8080/api/advertisements";

    private boolean isApprovedMode = false;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        sellerColumn.setCellValueFactory(new PropertyValueFactory<>("sellerName"));

        pendingTable.setItems(adList);

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

        Platform.runLater(this::loadPendingAdvertisements);
    }

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
        alert.setTitle("Advertisement Details");
        alert.setHeaderText("Viewing Ad ID: " + ad.getId());
        alert.setContentText("Title: " + ad.getTitle() + "\n" +
                "Price: $" + ad.getPrice() + "\n" +
                "Seller: " + ad.getSellerName());
        alert.showAndWait();
    }

    // 🟢 متد جدید خروج و پاک‌سازی سشن امنیتی کاربر
    @FXML
    private void handleLogout() {
        // ۱. خروج امن با صفر کردن متغیرهای وضعیت احراز هویت توکن
        NetworkClient.authToken = null;
        NetworkClient.userRole = null;

        // ۲. هدایت ادمین به صفحه ورود اصلی اپلیکیشن
        NavigationUtils.navigateTo(
                btnLogout,
                "/com/secondhand/frontend/view/login.fxml",
                "SecondHand Market - Login"
        );
    }
}