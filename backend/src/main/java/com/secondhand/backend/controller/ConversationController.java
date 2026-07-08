package com.secondhand.backend.controller;

import com.secondhand.backend.dto.ConversationDto;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.repository.UserRepository;
import com.secondhand.backend.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;
    private final UserRepository userRepository;

    // ایجاد چت جدید بر اساس شناسه آگهی و توکن کاربر لاگین شده
    @PostMapping("/ad/{advertisementId}")
    public ResponseEntity<?> createConversation(
            @PathVariable Long advertisementId,
            Principal principal
    ) {
        try {
            User currentUser = userRepository.findByUserName(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

            ConversationDto conversation = conversationService.createConversation(currentUser.getId(), advertisementId);
            return ResponseEntity.ok(conversation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // دریافت لیست کامل چت‌های کاربر فعلی
    @GetMapping("/my-chats")
    public ResponseEntity<List<ConversationDto>> getUserConversations(Principal principal) {
        try {
            User currentUser = userRepository.findByUserName(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

            List<ConversationDto> conversations = conversationService.getUserConversations(currentUser.getId());
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}