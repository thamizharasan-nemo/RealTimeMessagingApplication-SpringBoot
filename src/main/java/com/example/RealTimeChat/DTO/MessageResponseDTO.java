package com.example.RealTimeChat.DTO;

import com.example.RealTimeChat.model.Message;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponseDTO {
    private int messageId;
    private int conversationId;
    private int senderId;
    private String senderName;
    private String content;

    @Enumerated(EnumType.STRING)
    private Message.MessageType messageType;

    private int replayToMessageId;
    private String replayToContent;

    private Instant createdAt;
    private Instant editedAt;
    private String deletedBy;

    private List<MessageResponseDTO> repliesResponse = new ArrayList<>();

    // Change it as better in future
    private String pinned;
}
