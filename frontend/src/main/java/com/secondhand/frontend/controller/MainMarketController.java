package com.secondhand.frontend.controller;

import com.secondhand.frontend.model.AdItem;
import com.secondhand.frontend.network.NetworkClient;
import com.secondhand.frontend.util.NavigationUtils;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class MainMarketController {

    @FXML private TextField searchField;
    @FXML private ListView<AdItem> adListView;

    @FXML
    public void initialize() {
        // دریافت تمام آگهی‌های فعال در بدو ورود به صفحه
        fetchAdsFromServer("/advertisements/active", "");

        adListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                AdItem selectedAd = adListView.getSelectionModel().getSelectedItem();
                if (selectedAd != null) {
                    openAdDetail(selectedAd);
                }
            }
        });
    }

    private void openAdDetail(AdItem ad) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/secondhand/frontend/view/ad_details.fxml"));
            javafx.scene.Parent root = loader.load();

            AdDetailsController controller = loader.getController();
            controller.setAdData(ad);

            Stage stage = (Stage) adListView.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Ad Detail - " + ad.getTitle());
            stage.show();

        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleSearch() {
        String query = searchField.getText().trim();
        if (query.isBlank()) {
            fetchAdsFromServer("/advertisements/active", "");
        } else {
            // ساخت یک بدنه جی‌سان ساده برای ارسال کلیدواژه جست‌وجو به بک‌آند
            String jsonRequestBody = String.format("{\"query\":\"%s\"}", query);
            fetchAdsFromServer("/advertisements/search", jsonRequestBody);
        }
    }

    private void fetchAdsFromServer(String endpoint, String jsonBody) {
        adListView.getItems().clear();

        // استفاده از متد جدید و استاندارد ارتقا یافته در NetworkClient
        String response = NetworkClient.sendPostRequest(endpoint, jsonBody);

        if (response == null || response.isBlank() || response.startsWith("ERROR")) {
            System.err.println("Failed to fetch advertisements: " + response);
            return;
        }

        /*
         * 💡 نکته مهم برای آینده:
         * پاسخ بک‌آند شما به صورت JSON Array خواهد بود (مثلاً [{"id":1,"title":"... "}]).
         * برای اینکه فعلاً پروژه ارور ندهد و دوستت بتواند ظاهر را تست کند،
         * این بخش را موقتاً با پارسر ساده نگه می‌داریم؛ اما یادتان باشد بعد از مچ شدن کامل،
         * باید این پاسخ جی‌سان را با کتابخانه‌ای مثل Jackson یا Gson به شیء AdItem تبدیل کنید.
         */
        try {
            // در صورتی که بک‌آند هنوز دیتای خام تستی می‌فرستد، این بخش آن را هندل می‌کند
            if (!response.startsWith("[")) {
                String[] adsRaw = response.split(";");
                for (String adRaw : adsRaw) {
                    String[] tokens = adRaw.split("\\|");
                    if (tokens.length >= 6) {
                        AdItem item = new AdItem(tokens[0], tokens[1], tokens[2], tokens[3], tokens[4], tokens[5]);
                        adListView.getItems().add(item);
                    }
                }
            } else {
                // TODO: در گام‌های بعدی که بخش آگهی‌های بک‌آند را نهایی کردید،
                // جی‌سانِ پاسخ آرایه‌ای را در این قسمت پارس و به لیست اضافه می‌کنیم.
                System.out.println("JSON Array received from server: " + response);
            }
        } catch (Exception e) {
            System.err.println("Parsing error: " + e.getMessage());
        }
    }

    @FXML
    public void goToCreatAd() {
        // انتقال به صفحه ساخت آگهی در آینده
        NavigationUtils.navigateTo(searchField, "/com/secondhand/frontend/view/create_ad.fxml", "Create Advertisement");
    }

    @FXML
    public void handleLogout() {
        // حذف توکن هنگام خروج از حساب کاربری جهت حفظ امنیت
        NetworkClient.authToken = null;
        NavigationUtils.navigateTo(searchField, "/com/secondhand/frontend/view/login.fxml", "SecondHand Market - Login");
    }
}