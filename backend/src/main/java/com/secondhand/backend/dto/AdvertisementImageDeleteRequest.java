package com.secondhand.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Identifies one persisted image that the owner wants to remove. */
@Data
public class AdvertisementImageDeleteRequest {

    @NotBlank(message = "Image path is required")
    private String imagePath;
}
