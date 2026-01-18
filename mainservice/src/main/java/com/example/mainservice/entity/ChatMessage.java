package com.example.mainservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Column(name = "sender_id", nullable = false)
    private Integer senderId;

    @Column(name = "receiver_id", nullable = false)
    private Integer receiverId;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "type", length = 20)
    private String type; // TEXT, IMAGE, FILE, SYSTEM

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "is_read")
    private Boolean read = false;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (read == null) {
            read = false;
        }
    }
}