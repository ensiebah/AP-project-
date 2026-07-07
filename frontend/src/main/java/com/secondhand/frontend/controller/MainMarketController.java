package com.secondhand.frontend.controller;

import com.secondhand.frontend.network.NetworkClient;
import com.secondhand.frontend.util.NavigationUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class MainMarketController {

    // 🎯 المان‌های بازار عمومی
    @FXML private TextField searchField;
    @FXML private ListView<String> adListView;

    // 🔘 دکمه ورود به پنل ادمین
    @FXML private Button btnAdminPanel;

    /**
     * ⚡ نقطه شروع متد مدیریت لایف‌سایکل صفحه (Initialize)
     * وظیفه: این متد فقط یک‌بار پس از بالا آمدن ظاهر صفحه اجرا می‌شود و تمام تنظیمات اولیه را یکجا انجام می‌دهد.
     */
    @FXML
    public void initialize() {
        // ۱. پر کردن لیست آگهی‌های پیش‌فرض برای خالی نبودن صفحه در ارائه
        adListView.getItems().addAll(
                "iPhone 13 Pro Max - $900 [Active]",
                "MacBook Pro M1 - $1200 [Active]",
                "PlayStation 5 - $450 [Active]"
        );

        // ۲. مخفی کردن دکمه پنل ادمین به صورت پیش‌فرض (اصول دسترسی داک پروژه)
        if (btnAdminPanel != null) {
            btnAdminPanel.setVisible(false);
        }
    }

    /**
     * 🟢 وظیفه: هدایت کاربر به صفحه ثبت آگهی جدید
     */
    @FXML
    public void goToCreatAd() {
        NavigationUtils.navigateTo(searchField, "/com/secondhand/frontend/view/create_ad.fxml", "Post a New Advertisement");
    }

    /**
     * 🔍 وظیفه: انجام عملیات جست‌وجو در بازار
     */
    @FXML
    public void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isBlank()) {
            System.out.println("Search keyword is empty. Fetching all ads...");
            return;
        }
        System.out.println("Sending search request for keyword: " + keyword);
    }

    /**
     * 🔴 وظیفه: خارج کردن کاربر از حساب کاربری
     */
    @FXML
    public void handleLogout() {
        System.out.println("Logging out user...");
        NavigationUtils.navigateTo(searchField, "/com/secondhand/frontend/view/login.fxml", "Login");
    }

    /**
     * 👮‍♂️ وظیفه: کنترل داینامیک دکمه ناوبری ادمین بر اساس پاسخ نقش از بک‌اَند
     * @param userRole نقش کاربر ("ADMIN" یا "USER")
     */
    public void configureNavigationBasedOnRole(String userRole) {
        if (btnAdminPanel != null) {
            if ("ADMIN".equals(userRole)) {
                btnAdminPanel.setVisible(true);
                btnAdminPanel.setDisable(false);
            } else {
                btnAdminPanel.setVisible(false);
            }
        }
    }

    /**
     * 🔀 وظیفه: اکشن کلیک دکمه ادمین جهت باز کردن صفحه دو برگه‌ای ادمین
     */
    @FXML
    private void handleNavigateToAdmin() {
        // تغییر مسیر به صفحه FXML جدیدی که ساختی
        NavigationUtils.navigateTo(btnAdminPanel, "/com/secondhand/frontend/view/admin_panel.fxml", "Admin Dashboard");
    }
}