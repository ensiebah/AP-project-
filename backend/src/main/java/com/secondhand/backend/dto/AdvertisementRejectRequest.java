package com.secondhand.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Reason supplied by an administrator when an advertisement is rejected. */
@Data
public class AdvertisementRejectRequest {

    @NotBlank(message = "A rejection reason is required")
    @Size(max = 1000, message = "Rejection reason must not exceed 1000 characters")
    private String reason;
}
