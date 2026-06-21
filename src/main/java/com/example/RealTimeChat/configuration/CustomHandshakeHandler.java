package com.example.RealTimeChat.configuration;

import com.example.RealTimeChat.security.CustomUserDetails;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Component
public class CustomHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {

        Authentication auth = (org.springframework.security.authentication
                .UsernamePasswordAuthenticationToken) attributes.get("SPRING_SECURITY_CONTEXT");

        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails){
            // Principal name = userId string — matches convertAndSendToUser(String.valueOf(userId), ...)
            String userId = String.valueOf(userDetails.getUserId());
            System.out.println("\n============ "+ userId+" ==============\n");
            return () -> userId;

        }
        return null;
    }
}
