package com.secondhand.backend.dto;

import com.secondhand.backend.entity.AdvertisementStatus;
import lombok.*;
import java.util.List;

@Data
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdvertisementDto {

    private Long id;

    private String title;

    private String description;

    private Double price;

    private AdvertisementStatus status;

    private String rejectionReason;

    private Long sellerId;

    private String sellerName;

    private Long categoryId;

    private String categoryName;

    private Long cityId;

    private String cityName;

    private List<String> images;

    private List<AdvertisementImageDto> imageDetails;
}