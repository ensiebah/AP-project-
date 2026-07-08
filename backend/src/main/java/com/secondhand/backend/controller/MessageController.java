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

    // دریافت تاریخچه پیام‌های یک گفتگو
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<List<MessageDto>> getConversationMessages(
            @PathVariable Long conversationId
    ) {
        try {
            List<MessageDto> messages = messageService.getConversationMessages(conversationId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}