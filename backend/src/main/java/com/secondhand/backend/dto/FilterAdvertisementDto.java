package com.secondhand.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FilterAdvertisementDto {
    private String query;       // کلمه کلیدی (متنی)
    private Long categoryId;    // 👈 تغییر به آیدی برای انطباق با دیتابیس شما
    private Long cityId;        // 👈 تغییر به آیدی برای انطباق با دیتابیس شما
    private Double minPrice;
    private Double maxPrice;
}