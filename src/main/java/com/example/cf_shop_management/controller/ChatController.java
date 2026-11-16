package com.example.cf_shop_management.controller;

import com.example.cf_shop_management.dto.ApiResponse;
import com.example.cf_shop_management.service.ChatService;
import com.example.cf_shop_management.service.ChatService.ChatResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatController {

    ChatService chatService;

    public record ChatRequest(String sessionToken, Long accountId, String message) {}

    @PostMapping
    public ApiResponse<ChatResponse> chat(@RequestBody ChatRequest request) {
        return ApiResponse.<ChatResponse>builder()
                .result(chatService.chat(
                        request.sessionToken(),
                        request.accountId(),
                        request.message()
                ))
                .build();
    }
}