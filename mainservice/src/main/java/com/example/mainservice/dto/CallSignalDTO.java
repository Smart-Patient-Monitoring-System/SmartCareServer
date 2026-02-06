package com.example.mainservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallSignalDTO {
    private Long conversationId;
    private Long fromUserId;
    private Long toUserId;

    // OFFER, ANSWER, ICE, HANGUP
    private String type;

    // AUDIO, VIDEO
    private String callType;

    // offer/answer/ice object
    private Object payload;
}
