package com.example.mainservice.service;

import com.example.mainservice.dto.ChatMessageDTO;
import com.example.mainservice.entity.ChatMessage;
import com.example.mainservice.entity.Conversation;
import com.example.mainservice.repository.ChatMessageRepository;
import com.example.mainservice.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ConversationRepository conversationRepository;

    @Transactional
    public ChatMessage saveMessage(ChatMessageDTO messageDTO) {
        // Verify conversation exists
        Conversation conversation = conversationRepository
                .findById(messageDTO.getConversationId())
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // Create and save message
        ChatMessage chatMessage = ChatMessage.builder()
                .conversation(conversation)
                .senderId(messageDTO.getSenderId())
                .receiverId(messageDTO.getReceiverId())
                .content(messageDTO.getContent())
                .type(messageDTO.getType())
                .timestamp(LocalDateTime.now())
                .read(false)
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        // Update conversation's last message
        conversation.setLastMessage(messageDTO.getContent());
        conversation.setTimestamp(LocalDateTime.now());
        conversationRepository.save(conversation);

        log.info("Message saved with ID: {}", savedMessage.getId());
        return savedMessage;
    }

    public List<ChatMessage> getConversationMessages(Long conversationId) {
        return chatMessageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
    }

    @Transactional
    public void markMessagesAsRead(Long conversationId, Integer userId) {
        List<ChatMessage> unreadMessages = chatMessageRepository
                .findByConversationIdAndReceiverIdAndReadFalse(conversationId, userId);

        unreadMessages.forEach(msg -> msg.setRead(true));
        chatMessageRepository.saveAll(unreadMessages);

        log.info("Marked {} messages as read for conversation {}", unreadMessages.size(), conversationId);
    }

    public Integer getUnreadCount(Long conversationId, Integer userId) {
        return chatMessageRepository.countByConversationIdAndReceiverIdAndReadFalse(conversationId, userId);
    }

    // ========== MISSING METHODS - ADD THESE ==========

    /**
     * Get all conversations for a specific user (as patient or doctor)
     */
    public List<Conversation> getUserConversations(Integer userId) {
        return conversationRepository.findAllByUserId(userId);
    }

    /**
     * Create a new conversation between patient and doctor
     */
    @Transactional
    public Conversation createConversation(Integer patientId, Integer doctorId) {
        // Check if conversation already exists
        return conversationRepository.findByPatientIdAndDoctorId(patientId, doctorId)
                .orElseGet(() -> {
                    log.info("Creating new conversation between patient {} and doctor {}", patientId, doctorId);
                    Conversation newConversation = Conversation.builder()
                            .patientId(patientId)
                            .doctorId(doctorId)
                            .timestamp(LocalDateTime.now())
                            .build();
                    return conversationRepository.save(newConversation);
                });
    }

    /**
     * Get total unread message count for a user across all conversations
     */
    public Integer getTotalUnreadCount(Integer userId) {
        Integer count = chatMessageRepository.countByReceiverIdAndReadFalse(userId);
        return count != null ? count : 0;
    }
}