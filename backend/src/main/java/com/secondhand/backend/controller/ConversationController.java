package com.secondhand.backend.controller;

import com.secondhand.backend.dto.ConversationDto;
import com.secondhand.backend.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping
    public ConversationDto createConversation(
            @RequestParam Long buyerId,
            @RequestParam Long advertisementId
    ) {
        return conversationService.createConversation(
                buyerId,
                advertisementId
        );
    }

    @GetMapping("/user/{userId}")
    public List<ConversationDto> getUserConversations(
            @PathVariable Long userId
    ) {
        return conversationService.getUserConversations(userId);
    }
}