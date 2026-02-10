package com.example.RealTimeChat.controller;

import com.example.RealTimeChat.service.PresenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/presence")
public class PresenceController {

    private final PresenceService presenceService;

    public PresenceController(PresenceService presenceService) {
        this.presenceService = presenceService;
    }


    @GetMapping("/{userId}")
    public ResponseEntity<Boolean> isUserOnline(@PathVariable int userId) {
        return ResponseEntity.ok(presenceService.isOnline(userId));
    }

    @GetMapping("/all")
    public ResponseEntity<Set<Integer>> getAllOnlineUsers() {
        return ResponseEntity.ok(presenceService.getOnlineUsers());
    }

    @GetMapping("/all/redis")
    public ResponseEntity<List<Integer>> getAllOnlineUsersFromRedis() {
        return ResponseEntity.ok(presenceService.getOnlineUsersFromRedis());
    }

    @GetMapping("/all/conv/redis/{convId}")
    public ResponseEntity<List<Integer>> getAllOnlineUsersForConv(@PathVariable Integer convId) {
        return ResponseEntity.ok(presenceService.getAllOnlineUserInConv(convId));
    }
}