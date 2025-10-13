package com.example.RealTimeChat.service;

import com.example.RealTimeChat.DTO.ParticipantResponseDTO;
import com.example.RealTimeChat.exception.GenericNotFoundException;
import com.example.RealTimeChat.model.Conversation;
import com.example.RealTimeChat.model.ConversationParticipant;
import com.example.RealTimeChat.model.User;
import com.example.RealTimeChat.repository.ParticipantRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParticipantService {

    private final ParticipantRepository participantRepo;

    public ParticipantService(ParticipantRepository participantRepo) {
        this.participantRepo = participantRepo;
    }

    public List<ConversationParticipant> getAllParticipants() {
        return participantRepo.findAll();
    }

    public List<ParticipantResponseDTO> getAllParticipantsAsResponse() {
        return participantRepo.findAll().stream()
                .map(p -> {
                            ParticipantResponseDTO response = new ParticipantResponseDTO();
                            response.setPId(p.getCpId());
                            response.setRole(p.getParticipantRole().name());
                            response.setNickname(p.getUser().getNickname());
                            response.setUserId(p.getUser().getUserId());
                            response.setJoinedOn(p.getJoinedOn());
                            return response;
                        }
                ).toList();
    }

    public ConversationParticipant getParticipantByConversationAndUser(Conversation conversation, User user) {
        return participantRepo.findByConversationAndUser(conversation, user)
                .orElseThrow(() -> new GenericNotFoundException("Participant not a member in this group."));
    }

    public int getAdminsCountInConv(int conversationId){
        return participantRepo.countNoOfAdminsInConv(conversationId);
    }

    public int getMembersCountInConv(int conversationId){
        return participantRepo.getMembersCountInConv(conversationId);
    }

    public List<ParticipantResponseDTO> getAllParticipantInConv(int conversationId){
        return participantRepo.findAllParticipantsByConversationId(conversationId).stream()
                .map(p -> {
                            ParticipantResponseDTO response = new ParticipantResponseDTO();
                            response.setPId(p.getCpId());
                            response.setRole(p.getParticipantRole().name());
                            response.setNickname(p.getUser().getNickname());
                            response.setUserId(p.getUser().getUserId());
                            response.setJoinedOn(p.getJoinedOn());
                            return response;
                        }
                ).toList();
    }

}
