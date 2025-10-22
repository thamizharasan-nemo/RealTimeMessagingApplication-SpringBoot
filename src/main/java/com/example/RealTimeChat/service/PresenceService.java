package com.example.RealTimeChat.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class PresenceService {

    private final Map<Integer, Boolean> onlineUsers = new HashMap<>();

    public void connect(int userId){
        onlineUsers.put(userId, true);
    }

    public void disconnect(int userId){
        onlineUsers.remove(userId);
    }

    public boolean isOnline(int userId){
        return onlineUsers.containsKey(userId);
    }

    public Set<Integer> getOnlineUsers(){
        return onlineUsers.keySet();
    }
}
