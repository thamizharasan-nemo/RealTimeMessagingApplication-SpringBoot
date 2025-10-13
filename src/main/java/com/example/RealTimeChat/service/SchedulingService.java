package com.example.RealTimeChat.service;

import com.example.RealTimeChat.model.Message;
import com.example.RealTimeChat.repository.MessageRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SchedulingService {

    private final MessageRepository messageRepo;

    public SchedulingService(MessageRepository messageRepo) {
        this.messageRepo = messageRepo;
    }

    // runs every hour
    @Scheduled(fixedRate = 60 * 60 * 1000) // 60 min x 60 sec x 1000 milli = 1 hour
    public void deleteDisappearingMessages(){
        LocalDateTime now = LocalDateTime.now();
        List<Message> expired = messageRepo.findExpiredMessages(now);
        if(!expired.isEmpty()){
            // soft deletion
            expired.forEach(m -> messageRepo.delete(m));
        }
    }
}
