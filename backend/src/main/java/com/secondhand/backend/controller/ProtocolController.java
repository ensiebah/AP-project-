package com.secondhand.backend.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ProtocolController {

    /**
     * Handles simple protocol requests sent from the frontend.
     * This endpoint is mainly used for testing the communication
     * between the frontend and backend.
     *
     * @param payload the request payload received from the frontend
     * @return the protocol response
     */
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