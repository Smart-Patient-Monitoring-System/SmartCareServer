package com.example.mainservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConversationDTO {
    private Long id;
    private UserInfo patient;
    private UserInfo doctor;
    private String lastMessage;
    private LocalDateTime timestamp;
    private Integer unreadCount;
    private Boolean online;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo {
        private Integer id;
        private String name;
        private String avatar;
        private Boolean online;
        private String role;

        public UserInfo(Integer id, String name, String avatar, Boolean online) {
            this.id = id;
            this.name = name;
            this.avatar = avatar;
            this.online = online;
        }
    }
}