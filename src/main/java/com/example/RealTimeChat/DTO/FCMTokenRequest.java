package com.example.RealTimeChat.DTO;

import lombok.Data;

@Data
public class FCMTokenRequest {
    private int userId;
    private String fcmToken;
}
