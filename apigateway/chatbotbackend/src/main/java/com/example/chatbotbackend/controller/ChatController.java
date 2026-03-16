package com.example.chatbotbackend.controller;

import com.example.chatbotbackend.dto.ChatRequest;
import com.example.chatbotbackend.dto.ChatResponse;
import com.example.chatbotbackend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ChatController {
    private final ChatService chatService   ;

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        return new ChatResponse(
                chatService.getReply(request.getMessage())
        );
    }
}
