package com.secondhand.frontend.model;

public class CategoryItem {
    private final Long id;
    private final String name;
    private final int level;

    public CategoryItem(Long id, String name, int level) {
        this.id = id;
        this.name = name;
        this.level = level;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public String toString() {
        // ایجاد دندانه‌گذاری با کاراکترهای دشت برای نمایش زیبا در ComboBox
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < level; i++) {
            builder.append("   -- ");
        }
        builder.append(name);
        return builder.toString();
    }
}