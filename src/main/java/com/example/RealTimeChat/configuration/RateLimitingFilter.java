package com.example.RealTimeChat.configuration;

import com.example.RealTimeChat.service.RateLimitingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitingService rateLimitingService;

    public RateLimitingFilter(RateLimitingService rateLimitingService) {
        this.rateLimitingService = rateLimitingService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String userId = request.getHeader("X-User-Id"); // example header in frontend - replace with auth header after imple spring security
        String key = userId == null || userId.isBlank()
                ? "IP: " + request.getRemoteAddr()
                : "userId: " + userId;


        boolean allowed = rateLimitingService.tryConsume(key, 1);

        log.info(
                "Remaining tokens: {}",
                rateLimitingService.getRemainingTokens(key)
        );

        if(!allowed){
            long waitSec = rateLimitingService.getSecondsUntilRefill(key);
            String retryAt = DateTimeFormatter.RFC_1123_DATE_TIME
                    .format(ZonedDateTime.now().plusSeconds(waitSec));

            response.setStatus(429);
            response.setHeader("Retry After", waitSec +" sec");
            response.getWriter().write("Too Many Requests - limit exceeded, wait " + waitSec
                    + "seconds\nRetry exactly at " + retryAt);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
