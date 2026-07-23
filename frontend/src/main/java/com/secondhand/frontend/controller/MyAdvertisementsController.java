package com.secondhand.frontend.controller;

import com.secondhand.frontend.model.AdvertisementDto;
import com.secondhand.frontend.network.NetworkClient;
import com.secondhand.frontend.util.NavigationUtils;
import com.secondhand.frontend.util.UiTheme;
import com.secondhand.frontend.util.UiMotion;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region; // 🟢 رفع خطای نبودن کلاس Region
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

public class MyAdvertisementsController {

    @FXML private ListView<AdvertisementDto> myAdsListView;

    @FXML
    public void initialize() {
        setupCustomCellFactory();
        loadUserAdvertisements();
    }

    private void loadUserAdvertisements() {
        myAdsListView.getItems().clear();
        String response = NetworkClient.getMyAdvertisements();
        if (response != null && !response.startsWith("ERROR")) {
            try {
                JSONArray array = new JSONArray(response);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject json = array.getJSONObject(i);
                    AdvertisementDto dto = new AdvertisementDto();
                    dto.setId(json.getLong("id"));
                    dto.setTitle(json.getString("title"));
                    dto.setPrice(json.getDouble("price"));
                    dto.setDescription(json.optString("description", ""));
                    dto.setCityName(json.optString("cityName", "Unknown"));
                    dto.setCategoryName(json.optString("categoryName", "Unknown"));
                    dto.setCategoryId(json.optLong("categoryId", 1L));
                    dto.setCityId(json.optLong("cityId", 1L));
                    dto.setStatus(json.optString("status", "PENDING"));
                    dto.setRejectionReason(json.optString("rejectionReason", ""));
                    java.util.List<String> imagePaths = new java.util.ArrayList<>();
                    JSONArray images = json.optJSONArray("images");
                    if (images != null) {
                        for (int imageIndex = 0; imageIndex < images.length(); imageIndex++) {
                            String imagePath = images.optString(imageIndex, "");
                            if (!imagePath.isBlank()) {
                                imagePaths.add(imagePath);
                            }
                        }
                    }
                    dto.setImages(imagePaths);
                    myAdsListView.getItems().add(dto);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setupCustomCellFactory() {
        myAdsListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(AdvertisementDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    HBox rowContainer = new HBox(15);
                    rowContainer.setAlignment(Pos.CENTER_LEFT);
                    rowContainer.getStyleClass().add("row-card");
                    UiMotion.installCardMotion(rowContainer);

                    // بخش اطلاعات متنی آگهی
                    VBox textData = new VBox(4);
                    Label titleLbl = new Label("Title: " + item.getTitle());
                    titleLbl.getStyleClass().add("ad-title");

                    // اطلاعات جزیی آگهی (حذف وضعیت از این قسمت متنی چون قرار است دکمه شود)
                    Label metaLbl = new Label(String.format("Price: $%.2f | Category: %s | City: %s",
                            item.getPrice(), item.getCategoryName(), item.getCityName()));
                    metaLbl.getStyleClass().add("ad-meta");
                    textData.getChildren().addAll(titleLbl, metaLbl);
                    if ("REJECTED".equalsIgnoreCase(item.getStatus())
                            && item.getRejectionReason() != null
                            && !item.getRejectionReason().isBlank()) {
                        Label reasonLabel = new Label("Admin feedback: " + item.getRejectionReason());
                        reasonLabel.getStyleClass().add("rejection-reason");
                        reasonLabel.setWrapText(true);
                        textData.getChildren().add(reasonLabel);
                    }

                    // ایجاد فاصله انعطاف‌پذیر بین متن و دکمه‌ها
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    // 🟢 ۱. ساخت دکمه وضعیت (Status)
                    String status = item.getStatus() != null ? item.getStatus().toUpperCase() : "PENDING";
                    Button btnStatus = new Button("Status: " + status);

                    // تعیین استایل و رنگ دکمه بر اساس وضعیت آگهی
                    String statusStyle = "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-min-width: 120;";
                    if ("APPROVED".equals(status) || "ACCEPTED".equals(status)) {
                        btnStatus.setStyle(statusStyle + " -fx-background-color: #2ecc71;"); // سبز
                    } else if ("REJECTED".equals(status)) {
                        btnStatus.setStyle(statusStyle + " -fx-background-color: #e74c3c;"); // قرمز
                    } else {
                        btnStatus.setStyle(statusStyle + " -fx-background-color: #f39c12;"); // نارنجی برای Pending و بقیه حالات
                    }
                    btnStatus.setDisable(true); // دکمه فقط جنبه نمایشی دارد و کلیک نمی‌شود
                    // اگر تمایل داری کاربر بتواند روی آن کلیک کند، خط بالا را حذف کن و به آن setOnAction بده.

                    // ۲. دکمه ویرایش
                    Button btnEdit = new Button("Edit ⚙");
                    btnEdit.setStyle("-fx-background-color: #4a5568; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");
                    btnEdit.setOnAction(e -> openEditDialog(item));

                    // ۳. دکمه حذف
                    Button btnDelete = new Button("Delete 🗑");
                    btnDelete.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");
                    btnDelete.setOnAction(e -> handleAdvertisementDeletion(item.getId()));

                    // چیدمان دکمه‌ها در سمت راست سطر
                    HBox actionsBox = new HBox(8, btnStatus, btnEdit, btnDelete);
                    actionsBox.setAlignment(Pos.CENTER_RIGHT);

                    rowContainer.getChildren().addAll(textData, spacer, actionsBox);
                    setGraphic(rowContainer);
                }
            }
        });
    }

    private void handleAdvertisementDeletion(Long id) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to permanently delete this advertisement?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                String result = NetworkClient.deleteAdvertisement(id);
                if ("SUCCESS".equals(result)) {
                    loadUserAdvertisements();
                } else {
                    new Alert(Alert.AlertType.ERROR, "Deletion failed!").show();
                }
            }
        });
    }

    private void openEditDialog(AdvertisementDto dto) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/secondhand/frontend/view/edit_ad.fxml"));
            Parent root = loader.load();
            UiTheme.decorate(root);
            EditAdController controller = loader.getController();
            controller.setAdvertisementData(dto);

            Stage stage = (Stage) myAdsListView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Edit Advertisement - " + dto.getTitle());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleBackToMarket() {
        NavigationUtils.navigateTo(myAdsListView, "/com/secondhand/frontend/view/main_market.fxml", "SecondHand Market");
    }
}