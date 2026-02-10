package com.example.RealTimeChat.controller;

import com.example.RealTimeChat.DTO.FCMTokenRequest;
import com.example.RealTimeChat.model.User;
import com.example.RealTimeChat.service.FCMTokenService;
import com.example.RealTimeChat.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/fcm")
@CrossOrigin(origins = "*")
public class FCMController {

    private final FCMTokenService fcmTokenService;

    public FCMController(FCMTokenService fcmTokenService) {
        this.fcmTokenService = fcmTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerToken(@RequestBody FCMTokenRequest request) {
        fcmTokenService.registerToken(request.getUserId(), request.getFcmToken());
        return ResponseEntity.ok("Token registered successfully");
    }

    @DeleteMapping("/unregister")
    public ResponseEntity<String> unregisterToken(@RequestParam String token) {
        fcmTokenService.removeToken(token);
        return ResponseEntity.ok("Token removed successfully");
    }
}
