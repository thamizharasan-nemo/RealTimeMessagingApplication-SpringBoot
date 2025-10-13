package com.example.RealTimeChat.controller;

import com.example.RealTimeChat.DTO.PayloadDTO.BlockingDTO;
import com.example.RealTimeChat.model.Block;
import com.example.RealTimeChat.service.BlockedService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/block")
public class BlockController {

    private final BlockedService blockedService;

    public BlockController(BlockedService blockedService) {
        this.blockedService = blockedService;
    }

    @GetMapping("/block-list/{blockerId}")
    public ResponseEntity<List<Block>> blockUser(int blockerId){
        return ResponseEntity.ok(blockedService.findAllBlockedUsers(blockerId));
    }

    @PostMapping("/blocking")
    public ResponseEntity<?> blockUser(@RequestBody BlockingDTO blockingDTO){
        blockedService.blockUser(blockingDTO);
        return ResponseEntity.ok().body("User blocked successfully");
    }

    @PostMapping("/un-blocking")
    public ResponseEntity<?> unBlockUser(@RequestBody BlockingDTO blockingDTO){
        blockedService.unBlockUser(blockingDTO);
        return ResponseEntity.ok().body("User unblocked successfully");
    }
}
