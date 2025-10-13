package com.example.RealTimeChat.controller;

import com.example.RealTimeChat.DTO.ConversationResponseDTO;
import com.example.RealTimeChat.service.ConversationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conversations/private")
public class PrivateConvController {

    private final ConversationService conversationService;

    public PrivateConvController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping("/create/{userId1}/{userId2}")
    public ResponseEntity<ConversationResponseDTO> createPrivateConv(@PathVariable int userId1,
                                                                     @PathVariable int userId2){
        return ResponseEntity.ok(conversationService.createPrivateConversation(userId1,userId2));
    }
}
