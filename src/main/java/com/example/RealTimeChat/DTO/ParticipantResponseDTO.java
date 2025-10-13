package com.example.RealTimeChat.DTO;

import lombok.Data;

import java.time.Instant;

@Data
public class ParticipantResponseDTO {
    private int pId;
    private int userId;
    private String nickname;
    private String role;
    private Instant joinedOn;
}
