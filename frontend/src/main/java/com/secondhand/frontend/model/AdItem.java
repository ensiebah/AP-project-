package com.secondhand.frontend.model;

public class AdItem {
    private final String id;
    private final String title;
    private final String description;
    private final String price;
    private final String city;
    private final String category;
    private final Long sellerId;      // 🟢 اضافه شد برای امتیازدهی
    private final String sellerName;  // 🟢 اضافه شد برای نمایش نام فروشنده

    public AdItem(String id, String title, String description, String price, String city, String category, Long sellerId, String sellerName) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.city = city;
        this.category = category;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPrice() {
        return price;
    }

    public String getCity() {
        return city;
    }

    public String getCategory() {
        return category;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    @Override
    public String toString() {
        return title + " - $" + price + " [" + category + "] (" + city + ")";
    }
}