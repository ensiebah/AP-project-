package com.secondhand.frontend.controller;

import com.secondhand.frontend.network.NetworkClient;
import com.secondhand.frontend.util.NavigationUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class MainMarketController {

    // 🎯 این المان‌ها دقیقاً با fx:id های فایل FXML شما جفت شده‌اند
    @FXML private TextField searchField;
    @FXML private ListView<String> adListView;

    /**
     * ⚡ متد Initialize به صورت خودکار پس از بارگذاری FXML اجرا می‌شود.
     * فعلاً چند داده نمونه در لیست قرار می‌دهیم تا ظاهر صفحه در ارائه خالی نباشد.
     * در گام بعدی این بخش را به بک‌آند متصل می‌کنیم تا آگهی‌های واقعی دیتابیس را نشان دهد.
     */
    @FXML
    public void initialize() {
        adListView.getItems().addAll(
                "iPhone 13 Pro Max - $900 [Active]",
                "MacBook Pro M1 - $1200 [Active]",
                "PlayStation 5 - $450 [Active]"
        );
    }

    /**
     * 🟢 وظیفه: هدایت کاربر به صفحه ثبت آگهی جدید با کلیک روی دکمه Creat New Ad
     * 🎯 اکشن متناظر در FXML: onAction="#goToCreatAd"
     */
    @FXML
    public void goToCreatAd() {
        // آدرس‌دهی دقیق بر اساس پوشه view فرانت‌آند شما
        NavigationUtils.navigateTo(searchField, "/com/secondhand/frontend/view/create_ad.fxml", "Post a New Advertisement");
    }

    /**
     * 🔍 وظیفه: انجام عملیات جست‌وجو در بازار بر اساس عنوان یا توضیحات
     * 🎯 اکشن متناظر در FXML: onAction="#handleSearch"
     */
    @FXML
    public void handleSearch() {
        String keyword = searchField.getText().trim();

        if (keyword.isBlank()) {
            System.out.println("Search keyword is empty. Fetching all ads...");
            // اینجا بعداً متد دریافت همه آگهی‌ها را صدا می‌زنیم
            return;
        }

        System.out.println("Sending search request for keyword: " + keyword);
        // اِندپوینت بک‌آند شما برای سرچ: /api/advertisements/search?keyword=...
        // در گام بعدی منطق دریافت و آپدیت لایو لیست را اینجا می‌نویسیم
    }

    /**
     * 🔴 وظیفه: خارج کردن کاربر از حساب کاربری و بازگشت به صفحه ورود
     * 🎯 اکشن متناظر در FXML: onAction="#handleLogout"
     */
    @FXML
    public void handleLogout() {
        System.out.println("Logging out user...");
        // توکن احراز هویت را در صورت نیاز می‌توانید اینجا از NetworkClient پاک کنید

        // هدایت امن کاربر به صفحه لاگین در پوشه view
        NavigationUtils.navigateTo(searchField, "/com/secondhand/frontend/view/login.fxml", "Login");
    }
}