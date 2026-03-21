package com.example.RealTimeChat.security;

import com.example.RealTimeChat.exception.BadRequestException;
import com.example.RealTimeChat.exception.NotAllowedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


@Slf4j
public class SecurityUtils {

    private SecurityUtils() {}

    // Helper to get user principal
    private static CustomUserDetails getPrincipal(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")){
            throw new NotAllowedException("User is not authenticated");
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

    public static Integer getCurrentUserIdOrNull(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            log.info("User has not authenticated :(\nFAILED fetching userId");
            return null;
        }
        Object principal = auth.getPrincipal();

        if (principal instanceof CustomUserDetails user){
            return user.getUserId();
        }
        return null;
    }
}
