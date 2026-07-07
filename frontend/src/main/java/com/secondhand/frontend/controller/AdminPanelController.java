package com.secondhand.frontend.controller;

import com.secondhand.frontend.model.AdvertisementDto; // مطمئن شو دی‌تی‌او مشابه را در فرانت داری
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.io.IOException;
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

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObservableList<AdvertisementDto> pendingList = FXCollections.observableArrayList();
    private final String BASE_URL = "http://localhost:8080/api/admin/advertisements";

    @FXML
    public void initialize() {
        // 1. تنظیم ستون‌های جدول که بفهمند از کدام فیلد DTO داده را بردارند (اصول کپسوله‌سازی JavaFX)
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        sellerColumn.setCellValueFactory(new PropertyValueFactory<>("sellerName"));

        // اتصال لیست پویا به جدول
        pendingTable.setItems(pendingList);

        // 2. لود کردن داده‌ها از بک‌اَند به محض باز شدن صفحه
        loadPendingAdvertisements();
    }

    /**
     * 🔄 وظیفه: ارسال درخواست GET به بک‌اَند برای پر کردن جدول
     */
    private void loadPendingAdvertisements() {
        pendingList.clear();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/pending"))
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(responseBody -> {
                    // تبدیل جی‌سان دریافتی به پوجو (POJO) و اضافه کردن به لیست جدول
                    JSONArray jsonArray = new JSONArray(responseBody);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        AdvertisementDto dto = new AdvertisementDto();
                        dto.setId(obj.getLong("id"));
                        dto.setTitle(obj.getString("title"));
                        dto.setPrice(obj.getDouble("price"));
                        dto.setSellerName(obj.getString("sellerName"));

                        // بازگشت به ترد اصلی جاوااف‌ایکس برای آپدیت UI
                        javafx.application.Platform.runLater(() -> pendingList.add(dto));
                    }
                });
    }

    /**
     * 🟢 وظیفه: اکشن دکمه Approve برای تایید آگهی انتخاب شده
     */
    @FXML
    private void handleApprove() {
        AdvertisementDto selectedAd = pendingTable.getSelectionModel().getSelectedItem();
        if (selectedAd == null) return; // اگر چیزی انتخاب نشده بود کاری نکن

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + selectedAd.getId() + "/approve"))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        // حذف موفقیت‌آمیز از جدول فرانت‌اَند بدون نیاز به ریلود کل صفحه
                        javafx.application.Platform.runLater(() -> pendingList.remove(selectedAd));
                    }
                });
    }

    /**
     * 🔴 وظیفه: اکشن دکمه Reject برای رد آگهی انتخاب شده
     */
    @FXML
    private void handleReject() {
        AdvertisementDto selectedAd = pendingTable.getSelectionModel().getSelectedItem();
        if (selectedAd == null) return;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + selectedAd.getId() + "/reject"))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        javafx.application.Platform.runLater(() -> pendingList.remove(selectedAd));
                    }
                });
    }
}