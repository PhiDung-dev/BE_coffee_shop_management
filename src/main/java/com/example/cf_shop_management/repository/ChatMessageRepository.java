package com.example.cf_shop_management.repository;

import com.example.cf_shop_management.entity.ChatMessage;
import com.example.cf_shop_management.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatSessionOrderByIdAsc(ChatSession chatSession);
    void deleteByChatSession(ChatSession chatSession);
}