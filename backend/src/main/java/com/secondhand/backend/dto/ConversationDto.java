package com.secondhand.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

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
    private String opponentUsername;   // نام طرف مقابل گفت‌وگو
    private String lastMessageContent; // متن آخرین پیام
    private LocalDateTime lastMessageTime; // زمان آخرین پیام
}