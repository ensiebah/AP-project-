package com.secondhand.frontend.util;

import javafx.application.Platform;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextInputControl;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Applies the shared stylesheet and the selected display language to every
 * FXML root. The default language remains English; Persian is an RTL option.
 */
public final class UiTheme {

    private static boolean persian;
    private static final Map<String, String> EN_TO_FA = new LinkedHashMap<>();
    private static final Map<String, String> FA_TO_EN = new LinkedHashMap<>();

    static {
        add("SecondHand Market", "بازار دست‌دوم");
        add("Browse", "مشاهده بازار");
        add("My ads", "آگهی‌های من");
        add("Saved", "ذخیره‌شده‌ها");
        add("Messages", "پیام‌ها");
        add("Admin", "مدیریت");
        add("Sign out", "خروج");
        add("Search ads", "جست‌وجوی آگهی‌ها");
        add("Reset", "پاک‌کردن فیلتر");
        add("Category", "دسته‌بندی");
        add("Subcategory", "زیردسته");
        add("City", "شهر");
        add("Price", "قیمت");
        add("From", "از");
        add("To", "تا");
        add("Sort", "مرتب‌سازی");
        add("Latest ads", "آگهی‌های جدید");
        add("My Dashboard", "داشبورد من");
        add("My advertisements", "آگهی‌های من");
        add("Saved advertisements", "آگهی‌های ذخیره‌شده");
        add("Post an advertisement", "ثبت آگهی");
        add("Edit your advertisement", "ویرایش آگهی");
        add("Advertisement details", "جزئیات آگهی");
        add("Description", "توضیحات");
        add("Send", "ارسال");
        add("Close", "بستن");
        add("Cancel", "انصراف");
        add("Save changes", "ذخیره تغییرات");
        add("Create account", "ساخت حساب کاربری");
        add("Sign in", "ورود");
        add("Welcome back", "خوش آمدید");
        add("Username", "نام کاربری");
        add("Password", "رمز عبور");
        add("Full name", "نام و نام خانوادگی");
        add("Phone number", "شماره تلفن");
        add("Email address", "ایمیل");
        add("Administration", "مدیریت سامانه");
        add("Conversations and offers", "گفت‌وگوها و پیشنهادها");
        add("← Back to market", "بازگشت به بازار →");
        add("← Market", "بازار →");
        add("← Back", "بازگشت →");
        add("+ Post an ad", "+ ثبت آگهی");
        add("+ Post new ad", "+ ثبت آگهی جدید");
    }

    private UiTheme() {
    }

    private static void add(String english, String farsi) {
        EN_TO_FA.put(english, farsi);
        FA_TO_EN.put(farsi, english);
    }

    public static void setPersian(boolean usePersian) {
        persian = usePersian;
    }

    public static boolean isPersian() {
        return persian;
    }

    public static void decorate(Parent root) {
        String cssPath = UiTheme.class.getResource("/style.css").toExternalForm();
        if (!root.getStylesheets().contains(cssPath)) {
            root.getStylesheets().add(cssPath);
        }
        applyLanguage(root);
        UiMotion.install(root);
        Platform.runLater(() -> UiMotion.playPageEntrance(root));
    }

    public static void applyLanguage(Parent root) {
        root.setNodeOrientation(persian ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);
        translateNode(root);
    }

    private static void translateNode(Node node) {
        Map<String, String> dictionary = persian ? EN_TO_FA : FA_TO_EN;
        if (node instanceof Labeled labeled && labeled.getText() != null) {
            labeled.setText(dictionary.getOrDefault(labeled.getText(), labeled.getText()));
        }
        if (node instanceof TextInputControl input && input.getPromptText() != null) {
            input.setPromptText(dictionary.getOrDefault(input.getPromptText(), input.getPromptText()));
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                translateNode(child);
            }
        }
    }
}
