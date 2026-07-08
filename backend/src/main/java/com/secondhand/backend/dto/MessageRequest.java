package com.secondhand.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data

public class MessageRequest {
    @NotNull(message = "Conversation ID is required")
    private Long conversationId ;

    @NotBlank(message = "Message content cannot be empty")
    private String content ;
}
