package com.example.RealTimeChat.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("api")
public class TestMvcController {

    @GetMapping("/test")
    public String testing(){
        String now = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now());
        return "Testing chat app at " + now;
    }
}
