package com.example.mainservice.repository;

import com.example.mainservice.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Find all messages in a conversation ordered by timestamp
     */
    List<ChatMessage> findByConversationIdOrderByTimestampAsc(Long conversationId);

    /**
     * Find unread messages for a specific user in a conversation
     */
    List<ChatMessage> findByConversationIdAndReceiverIdAndReadFalse(
            Long conversationId,
            Integer receiverId
    );

    /**
     * Count unread messages for a specific user in a conversation
     */
    Integer countByConversationIdAndReceiverIdAndReadFalse(
            Long conversationId,
            Integer receiverId
    );

    /**
     * Find all unread messages for a specific user across all conversations
     */
    List<ChatMessage> findByReceiverIdAndReadFalse(Integer receiverId);

    /**
     * Count total unread messages for a user
     */
    Integer countByReceiverIdAndReadFalse(Integer receiverId);
}