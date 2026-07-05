package com.secondhand.backend.controller;

import com.secondhand.backend.dto.MessageDto;
import com.secondhand.backend.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // 👈 اصلاح: مدیریت خطاها با try-catch جهت جلوگیری از کرش کردن سواگر
    @PostMapping
    public ResponseEntity<?> sendMessage(
            @RequestParam Long senderId,
            @RequestParam Long conversationId,
            @RequestParam String content // کلاینت متن پیام را ارسال می‌کند
    ) {
        try {
            MessageDto message = messageService.sendMessage(senderId, conversationId, content);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

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