package com.example.cf_shop_management.service;

import com.example.cf_shop_management.entity.Account;
import com.example.cf_shop_management.entity.ChatSession;
import com.example.cf_shop_management.repository.AccountRepository;
import com.example.cf_shop_management.repository.ChatSessionRepository;
import com.example.cf_shop_management.repository.JpaChatMemoryRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final ChatSessionRepository sessionRepository;
    private final AccountRepository accountRepository;

    public ChatService(
            ChatClient.Builder builder,
            JpaChatMemoryRepository jpaChatMemoryRepository,
            ChatSessionRepository sessionRepository,
            AccountRepository accountRepository) {
        this.sessionRepository = sessionRepository;
        this.accountRepository = accountRepository;

        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(jpaChatMemoryRepository)
                .maxMessages(20)
                .build();

        this.chatClient = builder
                .defaultSystem("Bạn là trợ lý tư vấn của quán cà phê, trả lời thân thiện và ngắn gọn bằng tiếng Việt.")
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    public ChatResponse chat(String sessionToken, Long accountId, String userMessage) {
        String activeToken = (sessionToken == null || sessionToken.isBlank())
                ? UUID.randomUUID().toString()
                : sessionToken;

        ChatSession session = sessionRepository.findBySessionToken(activeToken)
                .orElseGet(() -> ChatSession.builder()
                        .sessionToken(activeToken)
                        .build());

        if (accountId != null && session.getAccount() == null) {
            Account account = accountRepository.findById(accountId).orElse(null);
            session.setAccount(account);
        }
        sessionRepository.save(session);

        String reply = chatClient.prompt()
                .user(userMessage)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, activeToken))
                .call()
                .content();

        return new ChatResponse(activeToken, reply);
    }

    public record ChatResponse(String sessionToken, String reply) {}
}