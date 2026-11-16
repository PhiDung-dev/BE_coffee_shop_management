package com.example.cf_shop_management.controller;

import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.service.ChatService;
import com.example.cf_shop_management.service.ChatService.ChatResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChatService chatService;

    @Test
    void chat_ExistingSession_Success() throws Exception {
        ChatController.ChatRequest request = new ChatController.ChatRequest(
                "session-token-123",
                1L,
                "Quán có món bạc xỉu không?"
        );

        ChatResponse serviceResponse = new ChatResponse(
                "session-token-123",
                "Dạ quán có Bạc Xỉu đá và nóng với giá 35.000đ ạ."
        );

        when(chatService.chat(eq("session-token-123"), eq(1L), eq("Quán có món bạc xỉu không?")))
                .thenReturn(serviceResponse);

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.sessionToken").value("session-token-123"))
                .andExpect(jsonPath("$.result.reply").value("Dạ quán có Bạc Xỉu đá và nóng với giá 35.000đ ạ."));
    }

    @Test
    void chat_NewSession_GeneratesToken_Success() throws Exception {
        ChatController.ChatRequest request = new ChatController.ChatRequest(
                null,
                null,
                "Quán mở cửa mấy giờ?"
        );

        ChatResponse serviceResponse = new ChatResponse(
                "generated-uuid-token",
                "Quán mở cửa từ 7:00 sáng đến 22:30 tối hàng ngày ạ."
        );

        when(chatService.chat(isNull(), isNull(), eq("Quán mở cửa mấy giờ?")))
                .thenReturn(serviceResponse);

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.sessionToken").value("generated-uuid-token"))
                .andExpect(jsonPath("$.result.reply").value("Quán mở cửa từ 7:00 sáng đến 22:30 tối hàng ngày ạ."));
    }

    @Test
    void chat_AccountNotFound_Returns4xx() throws Exception {
        ChatController.ChatRequest request = new ChatController.ChatRequest(
                "session-token-123",
                999L,
                "Xin chào"
        );

        when(chatService.chat(eq("session-token-123"), eq(999L), eq("Xin chào")))
                .thenThrow(new AppException(ErrorCode.ACCOUNT_NOT_EXISTED));

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }
}