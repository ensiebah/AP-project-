package com.secondhand.backend.controller;

import com.secondhand.backend.dto.ConversationDto;
import com.secondhand.backend.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    // 👈 اصلاح: استفاده از ResponseEntity برای مدیریت خطاهای احتمالی (مثل بلاک بودن)
    @PostMapping
    public ResponseEntity<?> createConversation(
            @RequestParam Long buyerId,
            @RequestParam Long advertisementId
    ) {
        try {
            ConversationDto conversation = conversationService.createConversation(buyerId, advertisementId);
            return ResponseEntity.ok(conversation); // بازگشت وضعیت 200 به همراه داده
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage()); // بازگشت وضعیت 400 در صورت بروز خطا
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ConversationDto>> getUserConversations(
            @PathVariable Long userId
    ) {
        try {
            List<ConversationDto> conversations = conversationService.getUserConversations(userId);
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}