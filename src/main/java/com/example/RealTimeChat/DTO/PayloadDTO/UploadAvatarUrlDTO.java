package com.example.RealTimeChat.DTO.PayloadDTO;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadAvatarUrlDTO {
    private int conversationId;
    private int userId;
    private MultipartFile file;
}
