package com.secondhand.frontend.controller;

import com.secondhand.frontend.model.AdvertisementDto;
import com.secondhand.frontend.network.NetworkClient;
import com.secondhand.frontend.util.NavigationUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EditAdController {

    @FXML private TextField titleField;
    @FXML private TextField priceField;
    @FXML private TextArea descriptionArea;
    @FXML private HBox imagesContainer;

    private AdvertisementDto targetDto;
    // 🟢 اصلاح اصلی: استفاده از لیست برای مدیریت چندین تصویر
    private final List<String> loadedImageUrls = new ArrayList<>();

    // متغیر کمکی برای ردیابی ایندکس آیتم در حال درگ
    private int draggedIndex = -1;

    /**
     * Injects the active advertisement DTO records and parses the integrated local image strings out of the description text blocks.
     */
    public void setAdvertisementData(AdvertisementDto dto) {
        this.targetDto = dto;
        titleField.setText(dto.getTitle());
        priceField.setText(String.valueOf(dto.getPrice()));

        loadedImageUrls.clear();
        String rawDesc = dto.getDescription();

        if (rawDesc != null && rawDesc.contains("[IMG_URL:")) {
            int startIndex = rawDesc.indexOf("[IMG_URL:");
            int endIndex = rawDesc.indexOf("]", startIndex);
            if (endIndex > startIndex) {
                String allUrls = rawDesc.substring(startIndex + 9, endIndex);
                // جداسازی تصاویر بر اساس کاما
                String[] splitUrls = allUrls.split(",");
                for (String url : splitUrls) {
                    if (!url.isBlank()) {
                        loadedImageUrls.add(url.trim());
                    }
                }
                descriptionArea.setText(rawDesc.substring(0, startIndex).trim());
            }
        } else {
            descriptionArea.setText(rawDesc);
        }
        renderImagesSection();
    }

    /**
     * Renders or flushes the horizontal image tray node containing attached media files alongside dedicated clear operations.
     */
    private void renderImagesSection() {
        imagesContainer.getChildren().clear();
        if (loadedImageUrls.isEmpty()) {
            imagesContainer.getChildren().add(new Label("No image attached to this advertisement."));
            return;
        }

        // رندر کردن هر تصویر به همراه منطق درگ و دراپ و دکمه حذف
        for (int i = 0; i < loadedImageUrls.size(); i++) {
            final int currentIndex = i;
            String url = loadedImageUrls.get(i);

            VBox singleImageWrapper = new VBox(5);
            singleImageWrapper.setAlignment(javafx.geometry.Pos.CENTER);
            singleImageWrapper.setStyle("-fx-border-color: #dcdde1; -fx-border-radius: 4; -fx-padding: 5; -fx-background-color: #ffffff;");

            ImageView preview = new ImageView();
            try {
                preview.setImage(new Image(url, true));
                preview.setFitWidth(110);
                preview.setFitHeight(90);
                preview.setPreserveRatio(true);
            } catch (Exception e) {
                preview.setImage(null);
            }

            Button btnDeleteImg = new Button("Remove ❌");
            btnDeleteImg.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand;");
            btnDeleteImg.setOnAction(e -> {
                // حذف تصویر مشخص بر اساس ایندکس آن
                loadedImageUrls.remove(currentIndex);
                renderImagesSection();
            });

            singleImageWrapper.getChildren().addAll(preview, btnDeleteImg);

            // 🟢 پیاده‌سازی منطق هوشمند Drag & Drop برای تغییر ترتیب عکس‌ها
            setupDragAndDropHandlers(singleImageWrapper, currentIndex);

            imagesContainer.getChildren().add(singleImageWrapper);
        }
    }

    /**
     * 🟢 تنظیم رویدادهای Drag & Drop بر روی کانتینر هر تصویر
     */
    private void setupDragAndDropHandlers(VBox wrapper, final int index) {
        // ۱. شروع عملیات درگ کردن
        wrapper.setOnDragDetected(event -> {
            draggedIndex = index;
            Dragboard db = wrapper.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            // ارسال متن فرضی برای پر شدن درگ‌بورد سیستم
            content.putString(String.valueOf(index));
            db.setContent(content);

            // افکت بصری: نیمه شفاف کردن آیتم در حال حرکت
            wrapper.setOpacity(0.5);
            event.consume();
        });

        // ۲. ورود آیتم درگ شده به حریم یک باکس دیگر
        wrapper.setOnDragOver(event -> {
            if (event.getGestureSource() != wrapper && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        // ۳. رها کردن (Drop) آیتم روی باکس مقصد
        wrapper.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString() && draggedIndex != -1) {
                // جابجایی جایگاه آیتم‌ها در لیست اصلی
                String targetUrl = loadedImageUrls.remove(draggedIndex);
                loadedImageUrls.add(index, targetUrl);
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();

            // بازسازی بخش تصاویر با ترتیب جدید
            if (success) {
                renderImagesSection();
            }
        });

        // ۴. پایان عملیات درگ (چه موفق چه ناموفق)
        wrapper.setOnDragDone(event -> {
            wrapper.setOpacity(1.0);
            draggedIndex = -1;
            event.consume();
        });
    }

    /**
     * Opens system file picker dialogues to seamlessly capture alternative target desktop image items.
     */
    @FXML
    public void handleAddNewPicture() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Upload New Image Asset(s)");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        // 🟢 تغییر به showOpenMultipleDialog برای امکان افزودن چندین عکس جدید همزمان
        Stage stage = (Stage) titleField.getScene().getWindow();
        List<File> selectedFiles = chooser.showOpenMultipleDialog(stage);

        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            for (File file : selectedFiles) {
                String url = file.toURI().toString();
                if (!loadedImageUrls.contains(url)) {
                    loadedImageUrls.add(url);
                }
            }
            renderImagesSection();
        }
    }

    /**
     * Recombines structural data modifications into unified network update requests.
     */
    @FXML
    public void handleUpdateAdvertisement() {
        String finalDescription = descriptionArea.getText().trim();

        // 🟢 بازسازی استرینگ متصل با کاما از لیست تصاویر برای ارسال به سرور
        if (!loadedImageUrls.isEmpty()) {
            StringBuilder imgUrlsBuilder = new StringBuilder();
            for (int i = 0; i < loadedImageUrls.size(); i++) {
                imgUrlsBuilder.append(loadedImageUrls.get(i));
                if (i < loadedImageUrls.size() - 1) {
                    imgUrlsBuilder.append(",");
                }
            }
            finalDescription += " [IMG_URL:" + imgUrlsBuilder.toString() + "]";
        }

        String jsonUpdate = String.format(
                java.util.Locale.US,
                "{\"title\":\"%s\",\"description\":\"%s\",\"price\":%.2f,\"categoryId\":%d,\"cityId\":%d}",
                titleField.getText().trim(),
                finalDescription,
                Double.parseDouble(priceField.getText().trim()),
                targetDto.getCategoryId() != null ? targetDto.getCategoryId() : 1L,
                targetDto.getCityId() != null ? targetDto.getCityId() : 1L
        );

        String networkResponse = NetworkClient.updateAdvertisementRaw(targetDto.getId(), jsonUpdate);
        if (networkResponse != null && !networkResponse.startsWith("ERROR")) {
            handleCancel();
        } else {
            new Alert(Alert.AlertType.ERROR, "Failed to apply updates onto the database server.").show();
        }
    }

    @FXML
    public void handleCancel() {
        NavigationUtils.navigateTo(titleField, "/com/secondhand/frontend/view/my_advertisements.fxml", "My Advertisements Dashboard");
    }
}