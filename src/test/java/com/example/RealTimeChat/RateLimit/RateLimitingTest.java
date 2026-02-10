package com.example.RealTimeChat.RateLimit;

import com.example.RealTimeChat.service.RateLimitingService;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


public class RateLimitingTest {

    static RateLimitingService rateLimitingService;

    @BeforeAll
    static void setup(){
        rateLimitingService = new RateLimitingService();
    }

    void allowsInitially_then_denies_when_exhausted(){
        String key = "test-user";

        rateLimitingService.resetBucket(key);
        int allowed = 0;
        for (int i = 0; i < 40; i++) {
            if (rateLimitingService.tryConsume(key, 5)) allowed++;
        }
        assertEquals(30, allowed);
        System.out.println(allowed);

        boolean next = rateLimitingService.tryConsume(key, 1);
        assertFalse(next, "Limit exceeds");
    }
}
