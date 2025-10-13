package com.example.RealTimeChat.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
    private  int userId;
    private String username;
    private String password;
    private String phoneNo;
    private String nickname;
    private Instant createdOn;
    private String bio;
    private String online;
    private String lastSeen;
}
