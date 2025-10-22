package com.example.RealTimeChat.configuration;

import com.example.RealTimeChat.model.User;
import com.example.RealTimeChat.service.PresenceService;
import com.example.RealTimeChat.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.time.Instant;
import java.util.Map;

@Component
@Slf4j
public class WebSocketEventListener {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final PresenceService presenceService;
    private final UserService userService;

    public WebSocketEventListener(SimpMessagingTemplate simpMessagingTemplate, PresenceService presenceService, UserService userService) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.presenceService = presenceService;
        this.userService = userService;
    }

    @EventListener
    public void connectUser(SessionConnectedEvent event){
        Principal user = event.getUser();
        if (user != null){
            int userId = Integer.parseInt(user.getName());
            presenceService.connect(userId);

            Logger log = LoggerFactory.getLogger(user.getName());
            log.info("User " + user.getName() + " connected");

            simpMessagingTemplate.convertAndSend("/topic/presence", userId + " is ONLINE");
        }
    }

    @EventListener
    public void disconnectUser(SessionDisconnectEvent event){
        Principal user = event.getUser();
        if(user != null){
            int userId = Integer.parseInt(user.getName());
            presenceService.disconnect(userId);

            log.info("User " + user.getName() + " disconnected");

            userService.disconnectUser(userId);

            User disconnectedUser = userService.getUserById(userId);

            Map<String, Object> payload = Map.of(
                    "userId", userId,
                    "status", "Offline",
                    "last seen", userService.lastSeenHelper(disconnectedUser)
            );

            simpMessagingTemplate.convertAndSend("/topic/presence", payload);
        }
    }
}
