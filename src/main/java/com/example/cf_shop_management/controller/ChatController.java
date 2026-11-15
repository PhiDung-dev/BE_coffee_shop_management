package com.example.cf_shop_management.controller;

import com.example.cf_shop_management.service.ChatService;
import com.example.cf_shop_management.service.ChatService.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService geminiChatService;

    public record ChatRequest(String sessionToken, Long accountId, String message) {}

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        return geminiChatService.chat(
                request.sessionToken(),
                request.accountId(),
                request.message()
        );
    }
}