package com.example.RealTimeChat.controller;

import com.example.RealTimeChat.DTO.PayloadDTO.BanUserDTO;
import com.example.RealTimeChat.model.BannedUser;
import com.example.RealTimeChat.service.BanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ban")
public class BanController {

    private final BanService banService;

    public BanController(BanService banService) {
        this.banService = banService;
    }

    @PostMapping("/banning")
    public ResponseEntity<?> banUser(@RequestBody BanUserDTO banUserDTO){
        banService.banUser(banUserDTO);
        return ResponseEntity.ok().body("Banned successfully");
    }

    @DeleteMapping("/un-banning")
    public ResponseEntity<?> unBanUser(@RequestParam int convId, @RequestParam int userId, @RequestParam int adminId, @RequestParam String reason){
        banService.unBanUser(convId, userId, adminId);
        return ResponseEntity.ok().body("unBanned successfully");
    }
}
