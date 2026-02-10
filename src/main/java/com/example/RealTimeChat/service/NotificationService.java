package com.example.RealTimeChat.service;

import com.example.RealTimeChat.DTO.MessageResponseDTO;
import com.example.RealTimeChat.DTO.NotificationDTO;
import com.example.RealTimeChat.model.Conversation;
import com.example.RealTimeChat.model.ConversationParticipant;
import com.example.RealTimeChat.model.FCMToken;
import com.example.RealTimeChat.model.User;
import com.example.RealTimeChat.repository.FCMTokenRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ConversationService convService;
    private final FCMTokenRepository fcmTokenRepo;

    public NotificationService(SimpMessagingTemplate simpMessagingTemplate, ConversationService convService, FCMTokenRepository fcmTokenRepository) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.convService = convService;
        this.fcmTokenRepo = fcmTokenRepository;
    }

    // WebSocket based notification
    // works only if the user is connected
    public void notifyNewMessage(Integer convId,
                                 Integer messageId,
                                 String senderName,
                                 String body,
                                 int senderId) {
        NotificationDTO notification = new NotificationDTO(
                "NEW_NOTIFICATION",
                convId,
                messageId,
                senderName,
                body,
                Instant.now()
        );

        Conversation conversation = convService.getConversationById(convId);

        List<ConversationParticipant> participants = conversation.getParticipants().stream().toList();

        for (ConversationParticipant p : participants) {
            if (p.getUser().getUserId() == senderId) continue;

            try {
                simpMessagingTemplate.convertAndSendToUser(
                        String.valueOf(p.getUser().getUserId()),
                        "/queue/notifications",
                        notification
                );
            } catch (Exception e) {
                 log.info("Failed to send notification to " + "\"" + p.getUser().getUserId() + "\"");
            }
        }
    }

    public void notifyParticipants(Conversation conversation, MessageResponseDTO message) {
        List<User> participants = new ArrayList<>(conversation.getParticipants()).stream()
                .map(cp -> cp.getUser())
                .filter(u -> u.getUserId() != message.getSenderId())
                .toList();

        List<FCMToken> tokens = fcmTokenRepo.findAllByUserIn(participants);

        String title = message.getSenderName();
        String body = message.getContent() != null
                ? message.getContent().substring(0, Math.min(40, message.getContent().length()))
                : "";

        for (FCMToken token : tokens) {
            sendFirebaseNotification(
                    token.getToken(),
                    title,
                    body
            );
        }
    }

    public void sendFirebaseNotification(String token, String title, String body){
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(notification)
                    .putData("Type", "MESSAGE")
                    .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        }
        catch (FirebaseMessagingException fe){
            log.info("Error while sending notification "+fe.getMessage());
        }
    }


}
