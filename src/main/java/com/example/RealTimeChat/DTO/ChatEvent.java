package com.example.RealTimeChat.DTO;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public class ChatEvent<T> {
    private String message;
    private T data;
}
