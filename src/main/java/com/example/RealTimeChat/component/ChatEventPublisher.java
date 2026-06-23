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

//    public void broadcastToConversation(
//            int conversationId,
//            int senderId,
//            Object payload
//    ) {
//        Conversation conversation = conversationService.getConversationById(conversationId);
//
//        if (conversation.getConversationType() == Conversation.ConversationType.GROUP) {
//            simpMessagingTemplate.convertAndSend(
//                    "/topic/conversation." + conversationId,
//                    payload
//            );
//        }
//        else {
//
//            int receiverId = conversationService.getReceiverId(conversationId, senderId);
//            simpMessagingTemplate.convertAndSendToUser(
//                    String.valueOf(receiverId),
//                    "/queue/conversation." + conversationId,
//                    payload
//            );
//
//            simpMessagingTemplate.convertAndSendToUser(
//                    String.valueOf(senderId),
//                    "/queue/conversation." + conversationId,
//                    payload
//            );
//        }
//    }

    public void broadcastToConversation(int conversationId, int senderId, Object payload) {

        // Always broadcast to the conversation topic — works for BOTH group and private
        // The old approach (convertAndSendToUser for private) silently failed because
        // Spring couldn't find sessions without a Principal on the WebSocket session.
        // Topic broadcast requires no Principal at all.
        simpMessagingTemplate.convertAndSend(
                "/topic/conversation." + conversationId,
                payload
        );
    }
}
