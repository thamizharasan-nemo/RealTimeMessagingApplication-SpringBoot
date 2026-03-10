package com.example.RealTimeChat.DTO;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;


@AllArgsConstructor
@RequiredArgsConstructor
public class ChatEvent<T> {
    private String message;
    private T data;
}
