package com.example.mainservice.controller;

import com.example.mainservice.dto.ChatMessageDTO;
import com.example.mainservice.entity.ChatMessage;
import com.example.mainservice.entity.Conversation;
import com.example.mainservice.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageDTO messageDTO) {
        try {
            log.info("Received message from user {} to user {}",
                    messageDTO.getSenderId(), messageDTO.getReceiverId());

            // Save message to database
            ChatMessage savedMessage = chatService.saveMessage(messageDTO);

            // Send to receiver's personal queue
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(messageDTO.getReceiverId()),
                    "/queue/messages",
                    savedMessage
            );

            // Also send back to sender for confirmation
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(messageDTO.getSenderId()),
                    "/queue/messages",
                    savedMessage
            );

            log.info("Message sent successfully: {}", savedMessage.getId());

        } catch (Exception e) {
            log.error("Error sending message: {}", e.getMessage());
        }
    }

    @MessageMapping("/chat.typing")
    public void sendTypingIndicator(@Payload ChatMessageDTO messageDTO) {
        messagingTemplate.convertAndSendToUser(
                String.valueOf(messageDTO.getReceiverId()),
                "/queue/typing",
                messageDTO.getSenderId()
        );
    }

    @MessageMapping("/chat.addUser")
    public void addUser(
            @Payload ChatMessageDTO messageDTO,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        // Add username in WebSocket session
        headerAccessor.getSessionAttributes().put("userId", messageDTO.getSenderId());
        log.info("User connected: {}", messageDTO.getSenderId());
    }
}