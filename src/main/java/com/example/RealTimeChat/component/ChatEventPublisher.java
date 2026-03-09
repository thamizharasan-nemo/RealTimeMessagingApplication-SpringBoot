package com.example.RealTimeChat.component;

import com.example.RealTimeChat.model.Conversation;
import com.example.RealTimeChat.service.ConversationService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class ChatEventPublisher {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ConversationService conversationService;

    public ChatEventPublisher(SimpMessagingTemplate simpMessagingTemplate, ConversationService conversationService) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.conversationService = conversationService;
    }

    public void broadcastToConversation(
            int conversationId,
            int senderId,
            Object payload
    ) {
        Conversation conversation = conversationService.getConversationById(conversationId);

        if (conversation.getConversationType() == Conversation.ConversationType.GROUP) {
            simpMessagingTemplate.convertAndSend(
                    "/topic/conversation." + conversationId,
                    payload
            );
        }
        else {

            int receiverId = conversationService.getReceiverId(conversationId, senderId);
            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(receiverId),
                    "/queue/conversation." + conversationId,
                    payload
            );

            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(senderId),
                    "/queue/conversation." + conversationId,
                    payload
            );
        }
    }
}
