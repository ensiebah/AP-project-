package com.secondhand.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Compact data set used by the administration overview cards. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardSummaryDto {
    private long totalAdvertisements;
    private long pendingAdvertisements;
    private long activeAdvertisements;
    private long rejectedAdvertisements;
    private long soldAdvertisements;
    private long deletedAdvertisements;
    private long totalUsers;
    private long blockedUsers;
    private long totalCities;
    private long totalCategories;
    private long rootCategories;
}
