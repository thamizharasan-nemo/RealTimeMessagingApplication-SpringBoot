package com.example.RealTimeChat.controller;

import com.example.RealTimeChat.DTO.ParticipantResponseDTO;
import com.example.RealTimeChat.model.ConversationParticipant;
import com.example.RealTimeChat.service.ParticipantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/participants")
public class ParticipantController {

    private final ParticipantService participantService;

    public ParticipantController(ParticipantService participantService) {
        this.participantService = participantService;
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<ConversationParticipant>> getAllParticipants(){
        return ResponseEntity.ok(participantService.getAllParticipants());
    }

    @GetMapping("/all")
    public ResponseEntity<List<ParticipantResponseDTO>> getAllParticipantsAsResponse(){
        return ResponseEntity.ok(participantService.getAllParticipantsAsResponse());
    }

    @GetMapping("/count/admin/{conversationId}")
    public ResponseEntity<Integer> getAdminCount(@PathVariable int conversationId){
        return ResponseEntity.ok(participantService.getAdminsCountInConv(conversationId));
    }

    @GetMapping("/all/participants/{conversationId}")
    public ResponseEntity<List<ParticipantResponseDTO>> AllParticipantInConv(@PathVariable int conversationId){
        return ResponseEntity.ok(participantService.getAllParticipantInConv(conversationId));
    }
}
