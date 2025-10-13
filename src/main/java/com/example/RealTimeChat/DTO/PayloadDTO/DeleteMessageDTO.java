package com.example.RealTimeChat.DTO.PayloadDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteMessageDTO {
    private int messageId;
    private int conversationId;
    private int userId;
}
