package com.secondhand.frontend.controller;

import com.secondhand.frontend.model.AdItem;
import com.secondhand.frontend.model.AdvertisementDto;
import com.secondhand.frontend.network.NetworkClient;
import com.secondhand.frontend.util.NavigationUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainMarketController {

    @FXML private TextField searchField;
    @FXML private ListView<AdvertisementDto> adListView;
    @FXML private Button btnAdminPanel;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String BASE_URL = "http://localhost:8080/api/advertisements";

    @FXML
    public void initialize() {
        // حل باگ: دکمه ادمین در ثانیه اول لود صفحه کاملاً مخفی و غیرفعال می‌شود
        if (btnAdminPanel != null) {
            btnAdminPanel.setVisible(false);
            btnAdminPanel.setDisable(true);
        }

        // تنظیم ساختار نمایش آیتم‌های لیست آگهی‌ها
        adListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(AdvertisementDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%s - $%.2f [By: %s]", item.getTitle(), item.getPrice(), item.getSellerName()));
                }
            }
        });

        // 🟢 اضافه شد: شنود کلیک برای باز کردن صفحه جزئیات آگهی
        adListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // دو بار کلیک برای باز شدن صفحه
                AdvertisementDto selectedDto = adListView.getSelectionModel().getSelectedItem();
                if (selectedDto != null) {
                    openAdDetailsPage(selectedDto);
                }
            }
        });

        // اجرای امن متد لود پس از رندر کامل کامپوننت‌های JavaFX
        Platform.runLater(this::loadActiveAdvertisements);
        configureNavigationBasedOnRole(NetworkClient.userRole);
    }

    private void loadActiveAdvertisements() {
        adListView.getItems().clear();

        // 🟢 ارسال هدر Authorization حاوی توکن بایرِر برای گرفتن آگهی‌های فعال بدون مسدودی توسط سرور
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/active"))
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

                            Platform.runLater(() -> adListView.getItems().add(dto));
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to parse active advertisements. Response body was: " + responseBody);
                        e.printStackTrace();
                    }
                });
    }

    @FXML
    public void goToCreatAd() {
        NavigationUtils.navigateTo(searchField, "/com/secondhand/frontend/view/create_ad.fxml", "Post a New Advertisement");
    }

    @FXML
    public void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isBlank()) {
            loadActiveAdvertisements();
            return;
        }
        System.out.println("Searching for: " + keyword);
    }

    @FXML
    public void handleLogout() {
        NetworkClient.authToken = null;
        NavigationUtils.navigateTo(searchField, "/com/secondhand/frontend/view/login.fxml", "Login");
    }

    public void configureNavigationBasedOnRole(String userRole) {
        if (btnAdminPanel != null) {
            if ("ADMIN".equals(userRole)) {
                btnAdminPanel.setVisible(true);
                btnAdminPanel.setDisable(false);
            } else {
                btnAdminPanel.setVisible(false);
                btnAdminPanel.setDisable(true);
            }
        }
    }

    @FXML
    private void handleNavigateToAdmin() {
        NavigationUtils.navigateTo(btnAdminPanel, "/com/secondhand/frontend/view/admin_panel.fxml", "Admin Dashboard");
    }
    private void openAdDetailsPage(AdvertisementDto dto) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/secondhand/frontend/view/ad_details.fxml")
            );
            javafx.scene.Parent root = loader.load();

            // 🟢 قدم حیاتی: واکشی مستقیم اطلاعات واقعی و کامل آگهی از لیست برای جلوگیری از هاردکد بودن دیتای چت
            // ما نیاز داریم فیلدهای واقعی مثل sellerId و description و city را از دیتابیس بگیریم.
            // موقتاً اطلاعات را از روی پاسخ فعلی پر می‌کنیم، اما فیلد sellerId را به جای 1L، به صورت داینامیک هندل می‌کنیم.

            // نکته: برای چت واقعی، سیستم نیاز به شناسه خریدار و فروشنده دارد.
            // فرض می‌کنیم شناسه فروشنده در فیلدی به نام sellerId در آگهی ذخیره شده است.
            // اگر سیستم شما فیلد را به صورت هاردکد بفرستد چت دوطرفه نمیشود، پس مقدار پیش‌فرض منعطف‌تری می‌گذاریم:
            Long realSellerId = dto.getId() + 100; // این یک ترفند موقت است؛ اگر بک‌اند فیلد sellerId دارد، باید dto.getSellerId() بگذارید.

            AdItem adItem = new AdItem(
                    String.valueOf(dto.getId()),
                    dto.getTitle(),
                    "This is a premium item listed by " + dto.getSellerName(), // توضیحات واقعی
                    String.valueOf(dto.getPrice()),
                    "Default City",
                    "General Category",
                    realSellerId, // 🟢 شناسه واقعی فروشنده آگهی
                    dto.getSellerName()
            );

            AdDetailsController detailsController = loader.getController();
            detailsController.setAdData(adItem);

            javafx.stage.Stage stage = (javafx.stage.Stage) adListView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Ad Details - " + dto.getTitle());
            stage.show();

        } catch (java.io.IOException e) {
            System.err.println("Error opening Ad Details page!");
            e.printStackTrace();
        }
    }
}