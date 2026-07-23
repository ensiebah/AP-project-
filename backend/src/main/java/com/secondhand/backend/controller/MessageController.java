package com.secondhand.backend.controller;

import com.secondhand.backend.dto.MessageDto;
import com.secondhand.backend.dto.MessageRequest;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.repository.UserRepository;
import com.secondhand.backend.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final UserRepository userRepository;

    // ارسال پیام جدید به صورت JSON Body به همراه ولیدیشن @Valid و بررسی JWT
    /**
     * Sends a new message in an existing conversation.
     * The sender is identified using the authenticated user's token.
     *
     * @param messageRequest the message information including conversation ID and content
     * @param principal the authenticated user
     * @return the created message
     */
    @PostMapping
    public ResponseEntity<?> sendMessage(
            @Valid @RequestBody MessageRequest messageRequest,
            Principal principal
    ) {
        try {
            User currentUser = userRepository.findByUserName(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

            MessageDto message = messageService.sendMessage(
                    currentUser.getId(),
                    messageRequest.getConversationId(),
                    messageRequest.getContent()
            );
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Retrieves the complete message history of a conversation.
     *
     * @param conversationId the conversation ID
     * @return a list of messages in the conversation
     */
    // دریافت تاریخچه پیام‌های یک گفتگو
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<List<MessageDto>> getConversationMessages(
            @PathVariable Long conversationId,
            Principal principal
    ) {
        try {
            User currentUser = userRepository.findByUserName(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
            List<MessageDto> messages = messageService.getConversationMessages(
                    currentUser.getId(), conversationId
            );
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * The receiver calls this endpoint only after messages have been rendered.
     * It is the durable source of truth for the sender's double check mark.
     */
    @PutMapping("/conversation/{conversationId}/seen")
    public ResponseEntity<Void> markConversationMessagesAsSeen(
            @PathVariable Long conversationId,
            Principal principal
    ) {
        try {
            User currentUser = userRepository.findByUserName(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
            messageService.markConversationMessagesAsSeen(currentUser.getId(), conversationId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}