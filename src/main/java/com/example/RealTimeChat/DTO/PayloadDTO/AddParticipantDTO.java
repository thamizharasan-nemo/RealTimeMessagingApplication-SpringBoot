package com.example.RealTimeChat.DTO.PayloadDTO;

import com.example.RealTimeChat.model.ParticipantRole;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddParticipantDTO {
    private int conversationId;
    private int userId;
    @Enumerated(EnumType.STRING)
    private ParticipantRole role;
    private int adminId;
}
