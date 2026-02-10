package com.example.RealTimeChat.configuration;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
public class UserInterceptor implements ChannelInterceptor {
    @Override
    // preSend lets us, inspect or modify the message before send
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // StompHeaderAccessor for accessing STOMP headers (like login, destination, command, etc.).
        StompHeaderAccessor headerAccessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if(StompCommand.CONNECT.equals(headerAccessor.getCommand())) {
            String login = headerAccessor.getFirstNativeHeader("login");
            if (login != null) {
                headerAccessor.setUser(() -> login);
            }
        }

        return message;
    }
}
