package com.secondhand.frontend.controller;

import com.secondhand.frontend.model.AdvertisementDto;
import com.secondhand.frontend.network.NetworkClient;
import com.secondhand.frontend.util.NavigationUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller responsible for editing existing advertisements.
 * It preserves the image-management behavior and adds the same category
 * hierarchy used by the create-ad form.
 */
public class EditAdController {

    @FXML private TextField titleField;
    @FXML private TextField priceField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<IdNamePair> parentCategoryComboBox;
    @FXML private ComboBox<IdNamePair> categoryComboBox;
    @FXML private ComboBox<IdNamePair> cityComboBox;
    @FXML private HBox imagesContainer;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private AdvertisementDto targetDto;
    // Display URLs may be backend /api/images/file/... URLs or temporary local previews.
    private final List<String> loadedImageUrls = new ArrayList<>();
    private final List<File> newImageFiles = new ArrayList<>();
    private final List<String> removedServerImagePaths = new ArrayList<>();
    private final java.util.Map<String, File> localFileByPreviewUrl = new java.util.HashMap<>();
    private int draggedIndex = -1;
    private boolean selectingExistingParent;

    @FXML
    public void initialize() {
        setupComboBoxConverters();
        categoryComboBox.setDisable(true);

        parentCategoryComboBox.valueProperty().addListener((observable, oldValue, selectedParent) -> {
            if (selectingExistingParent) {
                return;
            }

            categoryComboBox.getItems().clear();
            categoryComboBox.setValue(null);
            categoryComboBox.setDisable(selectedParent == null);
            if (selectedParent != null) {
                loadSubcategories(selectedParent.getId(), null);
            }
        });

        fetchDropdownData("/api/lookup/categories", parentCategoryComboBox, this::selectExistingCategory);
        fetchDropdownData("/api/lookup/cities", cityComboBox, this::selectExistingCity);
    }

    /**
     * Loads the selected advertisement into the editing form and selects its
     * current parent and child category once lookup data is available.
     */
    public void setAdvertisementData(AdvertisementDto dto) {
        this.targetDto = dto;
        titleField.setText(dto.getTitle());
        priceField.setText(String.valueOf(dto.getPrice()));

        loadedImageUrls.clear();
        newImageFiles.clear();
        removedServerImagePaths.clear();
        localFileByPreviewUrl.clear();

        String rawDesc = dto.getDescription() == null ? "" : dto.getDescription();
        // New ads carry server image URLs in dto.images, not hidden description text.
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            dto.getImages().forEach(path -> loadedImageUrls.add(NetworkClient.toAbsoluteImageUrl(path)));
            descriptionArea.setText(rawDesc);
        } else if (rawDesc.contains("[IMG_URL:")) {
            // Legacy compatibility for ads created before real uploads existed.
            int startIndex = rawDesc.indexOf("[IMG_URL:");
            int endIndex = rawDesc.indexOf("]", startIndex);
            if (endIndex > startIndex) {
                String allUrls = rawDesc.substring(startIndex + 9, endIndex);
                for (String url : allUrls.split(",")) {
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

        // initialize() may have already finished before this method is called.
        selectExistingCategory();
        selectExistingCity();
    }

    /**
     * The ad DTO contains the leaf id. This request obtains parentId so the
     * form can first select the parent and then load its child list.
     */
    private void selectExistingCategory() {
        if (targetDto == null || targetDto.getCategoryId() == null
                || parentCategoryComboBox.getItems().isEmpty()) {
            return;
        }

        HttpRequest request = authenticatedGet("/api/lookup/categories/" + targetDto.getCategoryId());
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(responseBody -> {
                    try {
                        JSONObject category = new JSONObject(responseBody);
                        Long parentId = category.isNull("parentId")
                                ? null : category.getLong("parentId");

                        Platform.runLater(() -> {
                            // If an old advertisement points to a root category, keep it
                            // visible but require the user to choose a subcategory before saving.
                            long rootId = parentId == null ? targetDto.getCategoryId() : parentId;
                            IdNamePair parent = findById(parentCategoryComboBox, rootId);
                            if (parent == null) {
                                return;
                            }

                            selectingExistingParent = true;
                            parentCategoryComboBox.setValue(parent);
                            selectingExistingParent = false;
                            loadSubcategories(parent.getId(), parentId == null ? null : targetDto.getCategoryId());
                        });
                    } catch (Exception e) {
                        System.err.println("Could not load the current category hierarchy.");
                    }
                });
    }

    private void selectExistingCity() {
        if (targetDto == null || targetDto.getCityId() == null || cityComboBox.getItems().isEmpty()) {
            return;
        }
        cityComboBox.setValue(findById(cityComboBox, targetDto.getCityId()));
    }

    private void loadSubcategories(long parentId, Long selectedChildId) {
        categoryComboBox.getItems().clear();
        categoryComboBox.setValue(null);
        categoryComboBox.setDisable(true);

        fetchDropdownData(
                "/api/lookup/categories/" + parentId + "/children",
                categoryComboBox,
                () -> {
                    categoryComboBox.setDisable(false);
                    if (selectedChildId != null) {
                        categoryComboBox.setValue(findById(categoryComboBox, selectedChildId));
                    }
                }
        );
    }

    private void fetchDropdownData(String endpoint, ComboBox<IdNamePair> comboBox, Runnable afterLoad) {
        HttpRequest request = authenticatedGet(endpoint);
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(responseBody -> {
                    try {
                        JSONArray array = new JSONArray(responseBody);
                        Platform.runLater(() -> {
                            comboBox.getItems().clear();
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                comboBox.getItems().add(new IdNamePair(obj.getLong("id"), obj.getString("name")));
                            }
                            afterLoad.run();
                        });
                    } catch (Exception e) {
                        System.err.println("Failed to parse lookup data from: " + endpoint);
                    }
                });
    }

    private HttpRequest authenticatedGet(String endpoint) {
        String token = NetworkClient.authToken != null ? NetworkClient.authToken : "";
        return HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080" + endpoint))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
    }

    private void setupComboBoxConverters() {
        StringConverter<IdNamePair> converter = new StringConverter<>() {
            @Override
            public String toString(IdNamePair item) {
                return item == null ? "" : item.getName();
            }

            @Override
            public IdNamePair fromString(String string) {
                return null;
            }
        };
        parentCategoryComboBox.setConverter(converter);
        categoryComboBox.setConverter(converter);
        cityComboBox.setConverter(converter);
    }

    private IdNamePair findById(ComboBox<IdNamePair> comboBox, long id) {
        return comboBox.getItems().stream()
                .filter(item -> item.getId() == id)
                .findFirst()
                .orElse(null);
    }

    private void renderImagesSection() {
        imagesContainer.getChildren().clear();
        if (loadedImageUrls.isEmpty()) {
            imagesContainer.getChildren().add(new Label("No image attached to this advertisement."));
            return;
        }

        for (int i = 0; i < loadedImageUrls.size(); i++) {
            final int currentIndex = i;
            String url = loadedImageUrls.get(i);

            VBox singleImageWrapper = new VBox(5);
            singleImageWrapper.setAlignment(javafx.geometry.Pos.CENTER);
            singleImageWrapper.setStyle("-fx-border-color: #dcdde1; -fx-border-radius: 4; -fx-padding: 5; -fx-background-color: #ffffff;");

            ImageView preview = new ImageView();
            try {
                NetworkClient.loadImageInto(preview, url);
                preview.setFitWidth(110);
                preview.setFitHeight(90);
                preview.setPreserveRatio(true);
            } catch (Exception e) {
                preview.setImage(null);
            }

            Button btnDeleteImg = new Button("Remove ❌");
            btnDeleteImg.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand;");
            btnDeleteImg.setOnAction(e -> {
                String removedUrl = loadedImageUrls.remove(currentIndex);
                File localFile = localFileByPreviewUrl.remove(removedUrl);
                if (localFile != null) {
                    newImageFiles.remove(localFile);
                } else {
                    String serverPath = toRelativeServerImagePath(removedUrl);
                    if (serverPath != null) {
                        removedServerImagePaths.add(serverPath);
                    }
                }
                renderImagesSection();
            });

            singleImageWrapper.getChildren().addAll(preview, btnDeleteImg);
            setupDragAndDropHandlers(singleImageWrapper, currentIndex);
            imagesContainer.getChildren().add(singleImageWrapper);
        }
    }

    private void setupDragAndDropHandlers(VBox wrapper, final int index) {
        wrapper.setOnDragDetected(event -> {
            draggedIndex = index;
            Dragboard db = wrapper.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(index));
            db.setContent(content);
            wrapper.setOpacity(0.5);
            event.consume();
        });

        wrapper.setOnDragOver(event -> {
            if (event.getGestureSource() != wrapper && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        wrapper.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString() && draggedIndex != -1) {
                String targetUrl = loadedImageUrls.remove(draggedIndex);
                loadedImageUrls.add(index, targetUrl);
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();

            if (success) {
                renderImagesSection();
            }
        });

        wrapper.setOnDragDone(event -> {
            wrapper.setOpacity(1.0);
            draggedIndex = -1;
            event.consume();
        });
    }

    @FXML
    public void handleAddNewPicture() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Upload New Image Asset(s)");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        Stage stage = (Stage) titleField.getScene().getWindow();
        List<File> selectedFiles = chooser.showOpenMultipleDialog(stage);

        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            for (File file : selectedFiles) {
                String previewUrl = file.toURI().toString();
                if (!localFileByPreviewUrl.containsKey(previewUrl)) {
                    loadedImageUrls.add(previewUrl);
                    localFileByPreviewUrl.put(previewUrl, file);
                    newImageFiles.add(file);
                }
            }
            renderImagesSection();
        }
    }

    @FXML
    public void handleUpdateAdvertisement() {
        if (targetDto == null) {
            showError("No advertisement was selected for editing.");
            return;
        }

        IdNamePair selectedSubcategory = categoryComboBox.getValue();
        IdNamePair selectedCity = cityComboBox.getValue();
        if (selectedSubcategory == null || selectedCity == null) {
            showError("Please select both a category and a subcategory.");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceField.getText().trim());
        } catch (NumberFormatException e) {
            showError("Price must be a valid number.");
            return;
        }

        String finalDescription = descriptionArea.getText().trim();
        String jsonUpdate = String.format(
                java.util.Locale.US,
                "{\"title\":%s,\"description\":%s,\"price\":%.2f,\"categoryId\":%d,\"cityId\":%d}",
                JSONObject.quote(titleField.getText().trim()),
                JSONObject.quote(finalDescription),
                price,
                selectedSubcategory.getId(),
                selectedCity.getId()
        );

        List<File> imagesToUpload = new ArrayList<>(newImageFiles);
        List<String> pathsToDelete = new ArrayList<>(removedServerImagePaths);
        Thread updateThread = new Thread(() -> {
            String networkResponse = NetworkClient.updateAdvertisementRaw(targetDto.getId(), jsonUpdate);
            int uploadFailures = 0;
            int deleteFailures = 0;
            if (networkResponse != null && !networkResponse.startsWith("ERROR")) {
                for (String imagePath : pathsToDelete) {
                    String deleteResponse = NetworkClient.deleteAdvertisementImage(targetDto.getId(), imagePath);
                    if (!"SUCCESS".equals(deleteResponse)) {
                        deleteFailures++;
                    }
                }
                for (File imageFile : imagesToUpload) {
                    String uploadResponse = NetworkClient.uploadAdvertisementImage(targetDto.getId(), imageFile);
                    if (uploadResponse == null || uploadResponse.startsWith("ERROR")) {
                        uploadFailures++;
                    }
                }
            }
            int finalUploadFailures = uploadFailures;
            int finalDeleteFailures = deleteFailures;
            Platform.runLater(() -> {
                if (networkResponse != null && !networkResponse.startsWith("ERROR")) {
                    if (finalUploadFailures > 0 || finalDeleteFailures > 0) {
                        new Alert(Alert.AlertType.WARNING,
                                "Advertisement updated, but some image changes could not be saved.").show();
                    }
                    handleCancel();
                } else {
                    showError("Failed to apply updates onto the database server.");
                }
            });
        }, "edit-ad-request-thread");
        updateThread.setDaemon(true);
        updateThread.start();
    }

    private String toRelativeServerImagePath(String url) {
        String prefix = "http://localhost:8080";
        if (url != null && url.startsWith(prefix + "/api/images/file/")) {
            return url.substring(prefix.length());
        }
        if (url != null && url.startsWith("/api/images/file/")) {
            return url;
        }
        return null;
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message).show();
    }

    @FXML
    public void handleCancel() {
        NavigationUtils.navigateTo(titleField, "/com/secondhand/frontend/view/my_advertisements.fxml", "My Advertisements Dashboard");
    }

    public static class IdNamePair {
        private final long id;
        private final String name;

        public IdNamePair(long id, String name) {
            this.id = id;
            this.name = name;
        }

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
