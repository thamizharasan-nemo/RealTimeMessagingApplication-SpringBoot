package com.example.RealTimeChat.controller;


import com.example.RealTimeChat.DTO.ChatMessageDTO;
import com.example.RealTimeChat.DTO.MessageResponseDTO;
import com.example.RealTimeChat.DTO.PayloadDTO.DeleteMessageDTO;
import com.example.RealTimeChat.DTO.PayloadDTO.RestoreMessageDTO;
import com.example.RealTimeChat.model.Message;
import com.example.RealTimeChat.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    MessageService messageService;

    @GetMapping("/dev/{messageId}")
    public ResponseEntity<Message> checkGetMessageById(@PathVariable int messageId) {
        return ResponseEntity.ok(messageService.getMessageById(messageId));
    }

    @GetMapping("/{messageId}")
    public ResponseEntity<Message> getMessageById(@PathVariable int messageId) {
        return ResponseEntity.ok(messageService.getMessageByIdWithDetails(messageId));
    }

    @GetMapping("/response/{messageId}")
    public ResponseEntity<MessageResponseDTO> getMessageResponseById(@PathVariable int messageId) {
        return ResponseEntity.ok(messageService.getMessageResponseById(messageId));
    }

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<List<MessageResponseDTO>> getAllMessagesInCOnv(@PathVariable int conversationId) {
        return ResponseEntity.ok(messageService.getAllMessagesByConvId(conversationId));
    }

    @GetMapping("/replies/{messageId}")
    public ResponseEntity<List<MessageResponseDTO>> getReplies(@PathVariable int messageId,
                                                               @RequestParam(defaultValue = "0") int offset,
                                                               @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(messageService.getRepliesAsPage(messageId, offset, limit));
    }

    @PostMapping("/save")
    public ResponseEntity<MessageResponseDTO> saveMessage(@ModelAttribute ChatMessageDTO messageDTO){
        return ResponseEntity.ok(messageService.saveMessage(messageDTO));
    }

    @GetMapping("/{conversationId}/user/{requesterId}")
    public Page<MessageResponseDTO> getMessages(@PathVariable int conversationId,
                                                @PathVariable int requesterId,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        return messageService.getMessages(conversationId, requesterId, page, size);
    }

    // For finding all non deleted & only replies of a message
    @GetMapping("/not-deleted/replies/{messageId}")
    public ResponseEntity<?> getNonDeletedMessageReplies(@PathVariable int messageId){
        return ResponseEntity.ok(messageService.getMessageReplies(messageId));
    }

    @DeleteMapping("/soft-delete")
    public ResponseEntity<?> softDeleteMessage(@RequestBody DeleteMessageDTO deleteMessageDTO) {
        messageService.softDeleteMessage(deleteMessageDTO);
        return ResponseEntity.ok().body("Message soft deleted!");
    }

    @PutMapping("/restore")
    public ResponseEntity<MessageResponseDTO> restoringMessage(@RequestBody RestoreMessageDTO restoreMessageDTO) {
        return ResponseEntity.ok(messageService.restoringMessage(restoreMessageDTO));
    }

    @DeleteMapping("/permanent-delete")
    public ResponseEntity<?> deleteMessagePermanently(@RequestBody DeleteMessageDTO deleteMessageDTO) {
        messageService.deleteMessagePermanently(deleteMessageDTO);
        return ResponseEntity.ok().body("Message deleted permanently!");
    }
}
