package com.example.RealTimeChat.DTO.PayloadDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateConversationDescriptionDTO {
    private int conversationId;
    private int userId;
    private String description;
}
