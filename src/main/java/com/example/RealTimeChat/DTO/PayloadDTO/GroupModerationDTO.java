package com.example.RealTimeChat.DTO.PayloadDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupModerationDTO {
    private int conversationId;
    private int targetUserId;
    private int moderatorId;
    private String action;
}
