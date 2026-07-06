package com.secondhand.backend.controller;

import com.secondhand.backend.dto.MessageDto;
import com.secondhand.backend.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public MessageDto sendMessage(
            @RequestParam Long senderId,
            @RequestParam Long conversationId,
            @RequestParam String content
    ) {
        return messageService.sendMessage(
                senderId,
                conversationId,
                content
        );
    }

    @GetMapping("/conversation/{conversationId}")
    public List<MessageDto> getConversationMessages(
            @PathVariable Long conversationId
    ) {
        return messageService.getConversationMessages(conversationId);
    }
}