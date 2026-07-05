package com.secondhand.backend.service;

import com.secondhand.backend.dto.MessageDto;

import java.util.List;

public interface MessageService {

    MessageDto sendMessage(
            Long senderId,
            Long conversationId,
            String content
    );

    List<MessageDto> getConversationMessages(
            Long conversationId
    );
}