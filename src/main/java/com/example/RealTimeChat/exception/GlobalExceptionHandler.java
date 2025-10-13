package com.example.RealTimeChat.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Optional;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ConversationNotFoundException.class)
    public String conversationNotFoundExceptionHandler(HttpServletRequest request, ConversationNotFoundException ce) {
        return "Error occurred: " + ce.getMessage() + " \n" + "Path: " + request.getRequestURI();
    }

    @ExceptionHandler(UserNotFoundException.class)
    public String userNotFoundExceptionHandler(HttpServletRequest request, UserNotFoundException ue) {
        return "Error occurred: " + ue.getMessage() + " \n" + "Path: " + request.getRequestURI();
    }

    @ExceptionHandler(BadRequestException.class)
    public String badRequestExceptionHandler(HttpServletRequest request, BadRequestException be) {
        return "Error occurred: " + be.getMessage() + " \n" + "Path: " + request.getRequestURI();
    }

    @ExceptionHandler(GenericNotFoundException.class)
    public String badRequestExceptionHandler(HttpServletRequest request, GenericNotFoundException gne) {
        return "Error occurred: " + gne.getMessage() + " \n" + "Path: " + request.getRequestURI() + "\n" + "TimeStamp: " + LocalDateTime.now();
    }

    @ExceptionHandler(ConcurrentModificationException.class)
    public String concurrentModificationExceptionHandler(HttpServletRequest request, ConcurrentModificationException cme) {
        return "Message: " + "ConcurrentModificationException" + "\n"
                + "Error occurred: " + cme.getMessage() + " \n"
                + "Path: " + request.getRequestURI() + "\n"
                + "TimeStamp: " + LocalDateTime.now();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex, HttpServletRequest request){
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of(
                        "message: ", Optional.ofNullable(ex.getMessage()).orElse("null error"),
                        "timeStamp: ", LocalDateTime.now(),
                        "status: ",500,
                        "error: ", "Internal Server Error",
                        "path: ", request.getRequestURI()
                )
        );
    }
}
