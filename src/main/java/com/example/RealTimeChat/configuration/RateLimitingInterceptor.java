package com.example.RealTimeChat.configuration;

import com.example.RealTimeChat.security.CustomUserDetails;
import com.example.RealTimeChat.service.RateLimitingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.security.Principal;


@Slf4j
@Component
public class RateLimitingInterceptor implements ChannelInterceptor {

    private final RateLimitingService rateLimitingService;

    public RateLimitingInterceptor(RateLimitingService rateLimitingService) {
        this.rateLimitingService = rateLimitingService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor
                .getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        if (StompCommand.SEND.equals(accessor.getCommand())) {
            Principal principal = accessor.getUser();

            if (principal == null){
                log.warn("Unauthenticated SEND attempted - message dropped");
                return null;
            }

            if (!(principal instanceof UsernamePasswordAuthenticationToken auth) ||
                    !(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
                log.warn("Invalid Principal type");
                return null;
            }

            String key = "ws:user:" +userDetails.getUserId();
            boolean allowed = rateLimitingService.tryConsume(key, 1);

            if (!allowed) {
                log.warn("Rate limit exceeded for user {}", userDetails.getUsername()+"\nUserID: "+ userDetails.getUserId());
                return null;
            }
        }
        return message;
    }
}
