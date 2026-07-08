package com.secondhand.frontend.dto;

import java.time.LocalDateTime;

public class MessageDto {
    private Long id;


    private String content;

    private LocalDateTime sentAt;

    private Long senderId;

    private String senderUsername;

    private Long conversationId;

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public Long getSenderId() {
        return senderId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public Long getConversationId() {
        return conversationId;
    }
}
