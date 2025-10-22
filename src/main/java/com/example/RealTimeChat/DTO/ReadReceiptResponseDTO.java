package com.example.RealTimeChat.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReadReceiptResponseDTO {
    int readReceiptId;
    int messageId;
    String readerName;
    Instant readAt;
}
