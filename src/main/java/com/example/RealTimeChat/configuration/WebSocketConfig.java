package com.example.RealTimeChat.configuration;

import com.example.RealTimeChat.security.JwtHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final RateLimitingInterceptor rateLimitingInterceptor;
    private final UserInterceptor userInterceptor;
    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    public WebSocketConfig(RateLimitingInterceptor rateLimitingInterceptor, UserInterceptor userInterceptor, JwtHandshakeInterceptor jwtHandshakeInterceptor) {
        this.rateLimitingInterceptor = rateLimitingInterceptor;
        this.userInterceptor = userInterceptor;
        this.jwtHandshakeInterceptor = jwtHandshakeInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[]{10000, 10000});
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .addInterceptors(jwtHandshakeInterceptor)
                .setAllowedOrigins("http://127.0.0.1:5500", "http://localhost:3000")
                .withSockJS();
    }

    // The inbound channel is where messages from clients (like CONNECT, SUBSCRIBE, SEND) arrive.
    // with custom interceptor message can be read or modified before sent to broker
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(userInterceptor, rateLimitingInterceptor);
    }
}
