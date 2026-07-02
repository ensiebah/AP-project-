package com.secondhand.backend.service;

import com.secondhand.backend.dto.ConversationDto;

import java.util.List;

public interface ConversationService {

    ConversationDto createConversation(
            Long buyerId,
            Long advertisementId
    );

    List<ConversationDto> getUserConversations(
            Long userId
    );
}