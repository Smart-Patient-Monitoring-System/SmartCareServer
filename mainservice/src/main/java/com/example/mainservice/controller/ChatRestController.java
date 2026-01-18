package com.example.mainservice.controller;

import com.example.mainservice.dto.ChatMessageDTO;
import com.example.mainservice.dto.ConversationDTO;
import com.example.mainservice.entity.ChatMessage;
import com.example.mainservice.entity.Conversation;
import com.example.mainservice.entity.User;
import com.example.mainservice.repository.UserRepository;
import com.example.mainservice.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Configure properly for production
public class ChatRestController {

    private final ChatService chatService;
    private final UserRepository userRepository; // Add this

    /**
     * Get all conversations for the current user
     */
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDTO>> getConversations(Authentication authentication) {
        Integer userId = getCurrentUserId(authentication);

        List<Conversation> conversations = chatService.getUserConversations(userId);
        List<ConversationDTO> conversationDTOs = conversations.stream()
                .map(conv -> mapToConversationDTO(conv, userId))
                .collect(Collectors.toList());

        return ResponseEntity.ok(conversationDTOs);
    }

    /**
     * Get all messages in a conversation
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<ChatMessageDTO>> getMessages(
            @PathVariable Long conversationId,
            Authentication authentication) {

        Integer userId = getCurrentUserId(authentication);

        List<ChatMessage> messages = chatService.getConversationMessages(conversationId);
        List<ChatMessageDTO> messageDTOs = messages.stream()
                .map(this::mapToChatMessageDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(messageDTOs);
    }

    /**
     * Mark messages as read
     */
    @PostMapping("/conversations/{conversationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long conversationId,
            Authentication authentication) {

        Integer userId = getCurrentUserId(authentication);
        chatService.markMessagesAsRead(conversationId, userId);

        return ResponseEntity.ok().build();
    }

    /**
     * Create a new conversation
     */
    @PostMapping("/conversations")
    public ResponseEntity<ConversationDTO> createConversation(
            @RequestBody ConversationDTO conversationDTO,
            Authentication authentication) {

        Integer userId = getCurrentUserId(authentication);

        // Get patient and doctor IDs from the DTO's UserInfo objects
        Integer patientId = conversationDTO.getPatient().getId();
        Integer doctorId = conversationDTO.getDoctor().getId();

        Conversation conversation = chatService.createConversation(patientId, doctorId);

        return ResponseEntity.ok(mapToConversationDTO(conversation, userId));
    }

    /**
     * Get unread message count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Integer> getUnreadCount(Authentication authentication) {
        Integer userId = getCurrentUserId(authentication);
        Integer count = chatService.getTotalUnreadCount(userId);
        return ResponseEntity.ok(count);
    }

    // Helper methods
    private Integer getCurrentUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() != null) {
            // Adjust this based on your User entity structure
            return Integer.parseInt(authentication.getName());
        }
        throw new RuntimeException("User not authenticated");
    }

    private ConversationDTO mapToConversationDTO(Conversation conv, Integer currentUserId) {
        ConversationDTO dto = new ConversationDTO();
        dto.setId(conv.getId());
        dto.setLastMessage(conv.getLastMessage());
        dto.setTimestamp(conv.getTimestamp());

        // Fetch patient details
        User patient = userRepository.findById(conv.getPatientId())
                .orElse(null);

        // Fetch doctor details
        User doctor = userRepository.findById(conv.getDoctorId())
                .orElse(null);

        // Set patient info
        if (patient != null) {
            dto.setPatient(new ConversationDTO.UserInfo(
                    patient.getId(),
                    patient.getName(),
                    patient.getProfilePicture(), // or getAvatar() - adjust based on your User entity
                    false, // You can implement online status tracking
                    "Patient"
            ));
        }

        // Set doctor info
        if (doctor != null) {
            dto.setDoctor(new ConversationDTO.UserInfo(
                    doctor.getId(),
                    doctor.getName(),
                    doctor.getProfilePicture(), // or getAvatar() - adjust based on your User entity
                    false, // You can implement online status tracking
                    doctor.getRole() != null ? doctor.getRole().toString() : "Doctor"
            ));
        }

        // Calculate unread count
        Integer unreadCount = chatService.getUnreadCount(conv.getId(), currentUserId);
        dto.setUnreadCount(unreadCount);
        dto.setOnline(false); // Implement online status if needed

        return dto;
    }

    private ChatMessageDTO mapToChatMessageDTO(ChatMessage msg) {
        return ChatMessageDTO.builder()
                .id(msg.getId())
                .conversationId(msg.getConversation().getId())
                .senderId(msg.getSenderId())
                .receiverId(msg.getReceiverId())
                .content(msg.getContent())
                .type(msg.getType())
                .timestamp(msg.getTimestamp())
                .read(msg.getRead())
                .build();
    }
}