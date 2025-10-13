package com.example.RealTimeChat.DTO;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

@Data
public class UserDTO {

    @NotNull(message = "Username is required")
    private String username;

    @NotNull(message = "Password is required")
    private String password;

    @Column(unique = true, nullable = false)
    @NotNull(message = "Phone number is required")
    @Pattern(regexp = "\\d{10}", message = "Phone No must be 10 digits")
    private String phoneNo;

    private String nickname;
    private MultipartFile imageFile;

    private String bio;
    private boolean online;
    private Instant lastSeen;
}
