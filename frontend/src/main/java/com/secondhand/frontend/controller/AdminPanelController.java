package com.secondhand.frontend.controller;

import com.secondhand.frontend.model.AdvertisementDto;
import com.secondhand.frontend.network.NetworkClient;
import com.secondhand.frontend.util.NavigationUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
    @FXML private Button btnBack;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObservableList<AdvertisementDto> pendingList = FXCollections.observableArrayList();
    private final String BASE_URL = "http://localhost:8080/api/advertisements";

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        sellerColumn.setCellValueFactory(new PropertyValueFactory<>("sellerName"));

        pendingTable.setItems(pendingList);

        // اجرای امن متد لود پس از رندر کامل کامپوننت‌های JavaFX
        Platform.runLater(this::loadPendingAdvertisements);
    }

    private void loadPendingAdvertisements() {
        if (NetworkClient.authToken == null) {
            System.err.println("Admin Token is null! Cannot load pending advertisements.");
            return;
        }

        pendingList.clear();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/pending"))
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

                            Platform.runLater(() -> pendingList.add(dto));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
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
                        Platform.runLater(() -> pendingList.remove(selectedAd));
                    }
                });
    }

    @FXML
    private void handleReject() {
        AdvertisementDto selectedAd = pendingTable.getSelectionModel().getSelectedItem();
        if (selectedAd == null) return;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + selectedAd.getId() + "/reject"))
                .header("Authorization", "Bearer " + NetworkClient.authToken)
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        Platform.runLater(() -> pendingList.remove(selectedAd));
                    }
                });
    }

    @FXML
    private void handleBackToMarket() {
        MainMarketController marketController = NavigationUtils.navigateTo(
                btnBack,
                "/com/secondhand/frontend/view/main_market.fxml",
                "SecondHand Market"
        );

        if (marketController != null) {
            marketController.configureNavigationBasedOnRole("ADMIN");
        }
    }
}