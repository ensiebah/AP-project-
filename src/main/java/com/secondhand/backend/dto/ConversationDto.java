package com.secondhand.backend.dto;

import lombok.*;

@Data
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDto {

    private Long id;

    private Long buyerId;

    private String buyerUsername;

    private Long sellerId;

    private String sellerUsername;

    private Long advertisementId;

    private String advertisementTitle;
}