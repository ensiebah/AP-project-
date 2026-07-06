package com.secondhand.backend.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ProtocolController {

    @PostMapping("/handle")
    public String handleFrontendRequest(@RequestBody String payload) {
        System.out.println("Received from frontend: " + payload);

        // یک پاسخ نمونه برای تست اتصال
        if (payload.startsWith("LOGIN")) {
            return "LOGIN_SUCCESS|USER";
        }
        return "SUCCESS|Connected to backend!";
    }
}