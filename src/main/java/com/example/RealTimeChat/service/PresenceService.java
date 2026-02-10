package com.example.RealTimeChat.service;

import com.example.RealTimeChat.model.Conversation;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PresenceService {

    private final Map<Integer, Boolean> onlineUsers = new HashMap<>();

    private final RedisTemplate<String, String> redisTemplate;
    private final String USER_KEY = "user:online:";
    private final ConversationService conversationService;

    public PresenceService(RedisTemplate<String, String> redisTemplate, ConversationService conversationService) {
        this.redisTemplate = redisTemplate;
        this.conversationService = conversationService;
    }

    public void connect(int userId){
        onlineUsers.put(userId, true);
    }

    public void disconnect(int userId){
        onlineUsers.remove(userId);
    }

    public boolean isOnline(int userId){
//        return onlineUsers.containsKey(userId);
        String key = USER_KEY + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public Set<Integer> getOnlineUsers(){
        return onlineUsers.keySet();
    }

    public List<Integer> getOnlineUsersFromRedis(){
        Set<String> usersList = redisTemplate.keys("user:online:*");
        log.info(usersList.size()+" Users are currently online");
        if (usersList.isEmpty()) return List.of();

        return usersList.stream()
                .map(user -> Integer.valueOf(user.replace(USER_KEY, "")))
                .toList();
    }

    public List<Integer> getAllOnlineUserInConv(Integer convId){
        Conversation conversation = conversationService.getConversationById(convId);

        List<Integer> allParticipants = conversation.getParticipants().stream()
                .map(p -> p.getUser().getUserId())
                .toList();

        Set<String> usersList = redisTemplate.keys("user:online:*");
        if (usersList.isEmpty()) return List.of();

        List<Integer> onlineUsers =  usersList.stream()
                .map(user -> Integer.valueOf(user.replace(USER_KEY, "")))
                .toList();

        return allParticipants.stream()
                .filter(uid -> onlineUsers.contains(uid))
                .toList();
    }

    public void markOnline(Integer userId){
        String key = USER_KEY + userId;
        redisTemplate.opsForValue().setGet(key, "ONLINE", Duration.ofSeconds(30));
        System.out.println("User "+ key+" is online");
    }

    public void markOffline(Integer userId){
        String key =  USER_KEY + userId;
        redisTemplate.delete(key);
    }

    public void refreshOnline(Integer userId){
        String key = USER_KEY + userId;
        redisTemplate.expire(key, Duration.ofSeconds(10));
    }
}
