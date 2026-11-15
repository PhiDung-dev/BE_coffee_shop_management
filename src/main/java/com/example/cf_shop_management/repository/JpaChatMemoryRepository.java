package com.example.cf_shop_management.repository;

import com.example.cf_shop_management.entity.ChatMessage;
import com.example.cf_shop_management.entity.ChatSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class JpaChatMemoryRepository implements ChatMemoryRepository {

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional(readOnly = true)
    public List<String> findConversationIds() {
        return sessionRepository.findAll().stream()
                .map(ChatSession::getSessionToken)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> findByConversationId(String sessionToken) {
        return sessionRepository.findBySessionToken(sessionToken)
                .map(session -> messageRepository.findByChatSessionOrderByIdAsc(session).stream()
                        .map(this::toSpringMessage)
                        .toList())
                .orElse(Collections.emptyList());
    }

    @Override
    @Transactional
    public void saveAll(String sessionToken, List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        ChatSession session = sessionRepository.findBySessionToken(sessionToken)
                .orElseGet(() -> sessionRepository.save(
                        ChatSession.builder()
                                .sessionToken(sessionToken)
                                .build()
                ));

        List<Message> existing = messageRepository.findByChatSessionOrderByIdAsc(session).stream()
                .map(this::toSpringMessage)
                .toList();

        int overlap = findOverlapCount(existing, messages);
        List<Message> newMessages = messages.subList(overlap, messages.size());

        List<ChatMessage> entitiesToSave = new ArrayList<>();
        for (Message msg : newMessages) {
            String text = msg.getText();
            if (text == null || text.isBlank()) {
                continue;
            }

            ChatMessage entity = ChatMessage.builder()
                    .chatSession(session)
                    .senderType(msg.getMessageType().name()) // "USER", "ASSISTANT", "SYSTEM"
                    .messageText(text)
                    .retrievedDocs(extractRetrievedDocs(msg))
                    .build();

            entitiesToSave.add(entity);
        }

        if (!entitiesToSave.isEmpty()) {
            messageRepository.saveAll(entitiesToSave);
        }
    }

    @Override
    @Transactional
    public void deleteByConversationId(String sessionToken) {
        sessionRepository.findBySessionToken(sessionToken).ifPresent(session -> {
            messageRepository.deleteByChatSession(session);
            sessionRepository.delete(session);
        });
    }

    private Message toSpringMessage(ChatMessage entity) {
        String content = entity.getMessageText() != null ? entity.getMessageText() : "";
        String type = entity.getSenderType() != null ? entity.getSenderType().toUpperCase() : "USER";

        return switch (type) {
            case "ASSISTANT" -> new AssistantMessage(content);
            case "SYSTEM" -> new SystemMessage(content);
            default -> new UserMessage(content);
        };
    }

    private String extractRetrievedDocs(Message msg) {
        Object docs = msg.getMetadata().get("qa_retrieved_documents");
        if (docs == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(docs);
        } catch (JsonProcessingException e) {
            return docs.toString();
        }
    }

    private int findOverlapCount(List<Message> existing, List<Message> incoming) {
        int maxCheck = Math.min(existing.size(), incoming.size());
        for (int k = maxCheck; k > 0; k--) {
            boolean matches = true;
            for (int i = 0; i < k; i++) {
                Message e = existing.get(existing.size() - k + i);
                Message inc = incoming.get(i);
                if (e.getMessageType() != inc.getMessageType()
                        || !Objects.equals(e.getText(), inc.getText())) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                return k;
            }
        }
        return 0;
    }
}