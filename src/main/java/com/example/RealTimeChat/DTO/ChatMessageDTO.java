package com.example.RealTimeChat.DTO;

import com.example.RealTimeChat.model.Message;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO {
    private int conversationId;
    private int senderId;
    private String content;

    @Enumerated(EnumType.STRING)
    private Message.MessageType messageType;

    private MultipartFile file;

    private int replyToMessageId;
}
