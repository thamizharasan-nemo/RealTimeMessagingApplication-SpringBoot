package com.example.RealTimeChat.service;

import com.example.RealTimeChat.DTO.PayloadDTO.BanUserDTO;
import com.example.RealTimeChat.exception.BadRequestException;
import com.example.RealTimeChat.exception.UserNotFoundException;
import com.example.RealTimeChat.model.*;
import com.example.RealTimeChat.repository.BanRepository;
import com.example.RealTimeChat.repository.ParticipantRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class BanService {

    private final BanRepository banRepo;
    private final ConversationService conversationService;
    private final ParticipantRepository participantRepo;

    public BanService(BanRepository banRepo, ConversationService conversationService, ParticipantRepository participantRepo) {
        this.banRepo = banRepo;
        this.conversationService = conversationService;
        this.participantRepo = participantRepo;
    }

    public BannedUser findByConvIdAndUserId(int convId, int userId){
        return banRepo.findByConversationIdAndUserId(convId, userId)
                .orElseThrow(() -> new UserNotFoundException("This user is not banned in this group"));
    }

    public void banUser(BanUserDTO banUserDTO){
        Conversation conversation = conversationService.getConversationById(banUserDTO.getConvId());
        ConversationParticipant target = participantRepo.findByConversation_ConversationIdUser_UserId(banUserDTO.getConvId(), banUserDTO.getTargetUserId());
        ConversationParticipant admin = participantRepo.findByConversation_ConversationIdUser_UserId(banUserDTO.getConvId(), banUserDTO.getAdminId());
        if(admin.getParticipantRole() != ParticipantRole.ADMIN){
            throw new BadRequestException("Admins can only ban users.");
        }
        if (target == null){
            throw new UserNotFoundException("User not found.");
        }
        if (banRepo.existsByConvIdAndUserId(banUserDTO.getConvId(), banUserDTO.getTargetUserId())){
            throw new BadRequestException("User already banned from this conversation.");
        }
        // delete participant
        participantRepo.deleteByConversation_ConversationIdAndUser_UserId(banUserDTO.getConvId(), banUserDTO.getTargetUserId());

        BannedUser bannedUser = BannedUser.builder()
                .conversation(conversation)
                .user(target.getUser())
                .bannedAt(Instant.now())
                .reason(banUserDTO.getReason())
                .bannedBy(admin.getUser().getUsername())
                .permanently(true)
                .build();
        banRepo.save(bannedUser);
    }

    public void unBanUser(int convId, int userId, int adminId){
        BannedUser bannedUser = findByConvIdAndUserId(convId, userId);
        ConversationParticipant admin = participantRepo.findByConversation_ConversationIdUser_UserId(convId, adminId);
        if(admin.getParticipantRole() != ParticipantRole.ADMIN){
            throw new BadRequestException("Admins can only unban users.");
        }
        banRepo.delete(bannedUser);
    }
}
