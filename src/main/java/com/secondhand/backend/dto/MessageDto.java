package com.secondhand.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {

    private Long id;

    @NotBlank(message = "Message cannot be empty")
    private String content;

    private LocalDateTime sentAt;

    private Long senderId;

    private String senderUsername;

    private Long conversationId;
}