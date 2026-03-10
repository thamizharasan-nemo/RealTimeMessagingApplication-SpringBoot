package com.example.RealTimeChat.security;

import com.example.RealTimeChat.exception.BadRequestException;
import com.example.RealTimeChat.exception.NotAllowedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


public class SecurityUtils {

    private SecurityUtils() {}

    // Helper to get user principal
    private static CustomUserDetails getPrincipal(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")){
            throw new NotAllowedException("User not authenticated");
        }

        Object principal = auth.getPrincipal();

        if (!(principal instanceof CustomUserDetails userDetails)){
            throw new BadRequestException("Invalid PRINCIPAL type!\nProvide valid information to authenticate user");
        }

        return userDetails;
    }


    public static int getUserId(){
        return getPrincipal().getUserId();
    }

    public static String getUsername(){
        return getPrincipal().getUsername();
    }
}
