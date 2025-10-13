package com.example.RealTimeChat.DTO;

import com.example.RealTimeChat.model.Conversation;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationResponseDTO {

    // Response constructor for returning "conversation is deleted"
    public ConversationResponseDTO(String response){
        this.title = response;
    }

    private Integer conversationId;
    private String title;
    private String description;
    private String convCreatorName;

    @Enumerated(EnumType.STRING)
    @NotBlank(message = "ConversationType must be PRIVATE OR GROUP")
    private Conversation.ConversationType conversationType;

    private String avatarUrl;
    private Instant createdAt;
    private List<ParticipantResponseDTO> participants;
}
