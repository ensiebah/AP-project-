package com.secondhand.frontend.model;

import java.util.ArrayList;
import java.util.List;

/** Lightweight advertisement model used by detail and favorites screens. */
public class AdItem {
    private final String id;
    private final String title;
    private final String description;
    private final String price;
    private final String city;
    private final String category;
    private final Long sellerId;
    private final String sellerName;
    private final List<String> imageUrls;

    public AdItem(String id, String title, String description, String price, String city,
                  String category, Long sellerId, String sellerName) {
        this(id, title, description, price, city, category, sellerId, sellerName, List.of());
    }

    public AdItem(String id, String title, String description, String price, String city,
                  String category, Long sellerId, String sellerName, List<String> imageUrls) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.city = city;
        this.category = category;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.imageUrls = imageUrls == null ? new ArrayList<>() : new ArrayList<>(imageUrls);
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPrice() { return price; }
    public String getCity() { return city; }
    public String getCategory() { return category; }
    public Long getSellerId() { return sellerId; }
    public String getSellerName() { return sellerName; }
    public List<String> getImageUrls() { return new ArrayList<>(imageUrls); }

    @Override
    public String toString() {
        return title + " - $" + price + " [" + category + "] (" + city + ")";
    }
}
