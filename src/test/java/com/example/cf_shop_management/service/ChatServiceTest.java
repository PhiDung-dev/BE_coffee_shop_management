package com.example.cf_shop_management.service;

import com.example.cf_shop_management.entity.Account;
import com.example.cf_shop_management.entity.ChatSession;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.repository.AccountRepository;
import com.example.cf_shop_management.repository.ChatSessionRepository;
import com.example.cf_shop_management.repository.JpaChatMemoryRepository;
import com.example.cf_shop_management.service.ChatService.ChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;

import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient.Builder chatClientBuilder;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    @Mock
    private JpaChatMemoryRepository jpaChatMemoryRepository;

    @Mock
    private ChatSessionRepository sessionRepository;

    @Mock
    private AccountRepository accountRepository;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.defaultSystem(anyString())).thenReturn(chatClientBuilder);
        when(chatClientBuilder.defaultAdvisors(any(Advisor.class))).thenReturn(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(chatClient);

        chatService = new ChatService(
                chatClientBuilder,
                jpaChatMemoryRepository,
                sessionRepository,
                accountRepository
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void chat_ExistingSessionWithoutNewAccount_DoesNotSaveSession_Success() {
        ChatSession existingSession = ChatSession.builder()
                .id(1L)
                .sessionToken("session-token-123")
                .build();

        when(sessionRepository.findBySessionToken("session-token-123"))
                .thenReturn(Optional.of(existingSession));
        when(chatClient.prompt()
                .user("Quán có món bạc xỉu không?")
                .advisors(any(Consumer.class))
                .call()
                .content())
                .thenReturn("Dạ quán có Bạc Xỉu đá và nóng với giá 35.000đ ạ.");

        ChatResponse response = chatService.chat("  session-token-123  ", null, "Quán có món bạc xỉu không?");

        assertNotNull(response);
        assertEquals("session-token-123", response.sessionToken());
        assertEquals("Dạ quán có Bạc Xỉu đá và nóng với giá 35.000đ ạ.", response.reply());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void chat_ExistingSessionAttachAccount_SavesSession_Success() {
        ChatSession existingSession = ChatSession.builder()
                .id(1L)
                .sessionToken("session-token-123")
                .account(null)
                .build();

        Account account = Account.builder()
                .id(5L)
                .email("customer@gmail.com")
                .build();

        when(sessionRepository.findBySessionToken("session-token-123"))
                .thenReturn(Optional.of(existingSession));
        when(accountRepository.findById(5L)).thenReturn(Optional.of(account));
        when(sessionRepository.save(existingSession)).thenReturn(existingSession);
        when(chatClient.prompt()
                .user("Gợi ý đồ uống ít ngọt")
                .advisors(any(Consumer.class))
                .call()
                .content())
                .thenReturn("Bạn có thể thử Americano hoặc Trà ô long nguyên lá ạ.");

        ChatResponse response = chatService.chat("session-token-123", 5L, "Gợi ý đồ uống ít ngọt");

        assertEquals("session-token-123", response.sessionToken());
        assertEquals(account, existingSession.getAccount());
        verify(sessionRepository).save(existingSession);
    }

    @Test
    @SuppressWarnings("unchecked")
    void chat_NewSessionWithAccount_GeneratesTokenAndSaves_Success() {
        Account account = Account.builder()
                .id(5L)
                .email("customer@gmail.com")
                .build();

        when(sessionRepository.findBySessionToken(anyString())).thenReturn(Optional.empty());
        when(accountRepository.findById(5L)).thenReturn(Optional.of(account));
        when(sessionRepository.save(any(ChatSession.class))).thenAnswer(inv -> inv.getArgument(0));
        when(chatClient.prompt()
                .user("Quán mở cửa mấy giờ?")
                .advisors(any(Consumer.class))
                .call()
                .content())
                .thenReturn("Quán mở cửa từ 7:00 đến 22:30 ạ.");

        ChatResponse response = chatService.chat(null, 5L, "Quán mở cửa mấy giờ?");

        assertNotNull(response);
        assertNotNull(response.sessionToken());
        assertFalse(response.sessionToken().isBlank());
        assertEquals("Quán mở cửa từ 7:00 đến 22:30 ạ.", response.reply());
        verify(sessionRepository).save(any(ChatSession.class));
    }

    @Test
    void chat_AccountNotFound_ThrowsException() {
        when(sessionRepository.findBySessionToken(anyString())).thenReturn(Optional.empty());
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> chatService.chat(null, 999L, "Xin chào"));

        assertEquals(ErrorCode.ACCOUNT_NOT_EXISTED, exception.getErrorCode());
        verify(sessionRepository, never()).save(any());
    }
}