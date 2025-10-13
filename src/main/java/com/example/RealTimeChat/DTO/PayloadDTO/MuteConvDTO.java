package com.example.RealTimeChat.DTO.PayloadDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MuteConvDTO {
    private int conversationId;
    private int userId;
}
