package com.example.RealTimeChat.configuration;

import com.example.RealTimeChat.model.User;
import com.example.RealTimeChat.security.CustomUserDetails;
import com.example.RealTimeChat.service.PresenceService;
import com.example.RealTimeChat.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

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

    // Just to add users in presence list, if online
    // added an end point to tell frontend that a user is online

    @EventListener
    public void connectUser(SessionConnectEvent event) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        UsernamePasswordAuthenticationToken auth =
                (UsernamePasswordAuthenticationToken) accessor.getSessionAttributes()
                        .get("SPRING_SECURITY_CONTEXT");

        if (auth == null) {
            log.warn("No authentication found in session attributes while connecting");
            return;
        }
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        int userId = userDetails.getUserId();
        String username = userDetails.getUsername();

        System.out.println("\nUser id of current user is " + userId + "\n");

        presenceService.connect(userId);
        presenceService.markOnline(userId); // redis key-value store for online users

        log.info("User " + username + " connected");

        simpMessagingTemplate.convertAndSend(
                "/topic/presence",
                "User " + userId + " is ONLINE");
    }

    @EventListener
    public void disconnectUser(SessionDisconnectEvent event) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(event.getMessage());

        UsernamePasswordAuthenticationToken auth =
                (UsernamePasswordAuthenticationToken) accessor.getSessionAttributes()
                        .get("SPRING_SECURITY_CONTEXT");

        if (auth == null) {
            log.warn("No authentication found in session attributes while connecting");
            return;
        }

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        int userId = userDetails.getUserId();
        String username = userDetails.getUsername();

        presenceService.disconnect(userId);

        presenceService.markOffline(userId);
        log.info("User " + username + " disconnected");

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
