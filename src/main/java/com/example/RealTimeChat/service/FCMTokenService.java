package com.example.RealTimeChat.service;

import com.example.RealTimeChat.exception.BadRequestException;
import com.example.RealTimeChat.model.FCMToken;
import com.example.RealTimeChat.model.User;
import com.example.RealTimeChat.repository.FCMTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class FCMTokenService {

    private final UserService userService;
    private final FCMTokenRepository fcmTokenRepo;

    public FCMTokenService(UserService userService, FCMTokenRepository fcmTokenRepos) {
        this.userService = userService;
        this.fcmTokenRepo = fcmTokenRepos;
    }

    public void registerToken(Integer userId, String token){
        if(token == null || token.isBlank()){
            throw new BadRequestException("Token is missing");
        }
        User user = userService.getUserById(userId);
        fcmTokenRepo.findByUser(user).ifPresentOrElse(
                existing -> {
                    existing = new FCMToken();
                    existing.setUser(user);
                    existing.setToken(token);
                    fcmTokenRepo.save(existing);
                },
                () -> {
                    FCMToken fcmToken = FCMToken.builder()
                            .token(token)
                            .user(user)
                            .build();
                    fcmTokenRepo.save(fcmToken);
                }
        );
        log.info("Token registered -> " + token);
    }

    public List<String> getUserTokens(Integer userId) {
        return fcmTokenRepo.findByUser_UserId(userId)
                .stream()
                .map(FCMToken::getToken)
                .toList();
    }

    public void removeToken(String token) {
        fcmTokenRepo.deleteByToken(token);
    }
}
