package com.example.RealTimeChat.exception;

public class ConversationNotFoundException extends RuntimeException{
    public ConversationNotFoundException(String message){
        super(message);
    }
}
