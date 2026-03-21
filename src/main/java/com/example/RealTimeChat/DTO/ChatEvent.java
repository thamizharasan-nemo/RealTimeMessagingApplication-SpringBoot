package com.example.RealTimeChat.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class ChatEvent<T> {
    private String type;
    private T payload;
}
