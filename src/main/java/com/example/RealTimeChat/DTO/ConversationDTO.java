package com.example.RealTimeChat.DTO;

import com.example.RealTimeChat.model.Conversation;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationDTO {

    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    @NotBlank(message = "ConversationType must be PRIVATE OR GROUP")
    private Conversation.ConversationType conversationType;

    @NotNull(message = "Group creator id is required")
    private int convCreatorId;
    private Set<Integer> participantIds;

}
