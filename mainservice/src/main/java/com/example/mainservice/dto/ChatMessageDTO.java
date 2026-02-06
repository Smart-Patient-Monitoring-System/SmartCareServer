package com.example.mainservice.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageDTO {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String content;
    private List<ChatAttachmentDTO> attachments;
    private String type; // TEXT, IMAGE, FILE, SYSTEM
    private LocalDateTime timestamp;
    private Boolean read;

}