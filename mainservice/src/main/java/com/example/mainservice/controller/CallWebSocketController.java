package com.example.mainservice.controller;

import com.example.mainservice.dto.CallSignalDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CallWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    // client sends: /app/call.signal
    @MessageMapping("/call.signal")
    public void handleCallSignal(@Payload CallSignalDTO dto) {
        if (dto == null || dto.getConversationId() == null) return;

        String destination = "/topic/call/" + dto.getConversationId();

        log.info("CALL {} {} -> {} (conv {}) to {}",
                dto.getType(), dto.getFromUserId(), dto.getToUserId(),
                dto.getConversationId(), destination);

        messagingTemplate.convertAndSend(destination, dto);
    }
}
