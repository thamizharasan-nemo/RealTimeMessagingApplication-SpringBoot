package com.example.RealTimeChat.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {
    private String type;
    private Integer conversationId;
    private Integer messageId;
    private String title;
    private String body;
    private Instant timestamp;

}
