package com.secondhand.frontend.model;

public class AdItem {
    private final String id;
    private final String title;
    private final String description;
    private final String price;
    private final String city;
    private final String category;

    public AdItem(String id, String title, String description, String price, String city, String category) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.city = city;
        this.category = category;
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
    @Override
    public String toString(){
        return title+" - $"+price+" ["+category+"] ("+city+")" ;
    }
}