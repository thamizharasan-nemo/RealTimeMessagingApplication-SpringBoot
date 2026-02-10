package com.example.RealTimeChat.configuration;

import com.example.RealTimeChat.service.RateLimitingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RateLimitingInterceptor implements ChannelInterceptor {

    private final RateLimitingService rateLimitingService;
    public RateLimitingInterceptor(RateLimitingService rateLimitingService) {
        this.rateLimitingService = rateLimitingService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor headerAccessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (headerAccessor == null) return message;

        if (StompCommand.SEND.equals(headerAccessor.getCommand())){
            String user = (headerAccessor.getUser() != null) ? headerAccessor.getUser().getName() : null;

            if(user == null){
                String headerUser = headerAccessor.getFirstNativeHeader("X-User_Id");
                user = (headerUser != null) ? headerUser : "Anonymous user";
            }

            String key = "ws:user:" + user;
            boolean allowed = rateLimitingService.tryConsume(key, 1);

            if (!allowed){
                log.warn("Rate limiting exceeds for user {}", user);
                throw new MessagingException("Rate limiting exceeds!");
            }
        }
        return message;
    }
}
