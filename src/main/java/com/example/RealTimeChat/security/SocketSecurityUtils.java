package com.example.RealTimeChat.security;

import com.example.RealTimeChat.exception.NotAllowedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.security.Principal;

public class SocketSecurityUtils {

    private SocketSecurityUtils() {
    }

    public static CustomUserDetails getUser(Principal principal) {
        if (principal == null) {
            throw new NotAllowedException("User not authenticated - Principal is null");
        }

        if (principal instanceof UsernamePasswordAuthenticationToken auth &&
                auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails;
        }

        throw new NotAllowedException("Invalid user");
    }

    public static int getUserId(Principal principal) {
        return getUser(principal).getUserId();
    }
}
