package com.example.RealTimeChat.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Table(name = "fcm_tokens")
@AllArgsConstructor
@Builder
public class FCMToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer tokenId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String token;

    private LocalDateTime createdAt;

    public FCMToken(){
        this.createdAt = LocalDateTime.now();
    }
}
