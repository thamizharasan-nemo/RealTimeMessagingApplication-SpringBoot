package com.example.RealTimeChat.RateLimit;

import com.example.RealTimeChat.configuration.RateLimitingFilter;
import com.example.RealTimeChat.service.RateLimitingService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class RateLimitingFilterTest {

    RateLimitingService rateLimitingService;
    RateLimitingFilter rateLimitingFilter;

    @BeforeEach
    void setup(){
        rateLimitingService = new RateLimitingService();
        rateLimitingFilter = new RateLimitingFilter(rateLimitingService);
    }

    @Test
    void httpFilter_returns429_when_exceeded() throws Exception{
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setMethod("POST");
        mockRequest.setRequestURI("api/sendMessage");
        mockRequest.addHeader("X-User-Id", "test-user");

        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        for (int i = 0; i < 30; i++) {
            rateLimitingFilter.doFilter(mockRequest, mockResponse, chain);
            mockResponse = new MockHttpServletResponse();
        }
        MockHttpServletResponse response2 = new MockHttpServletResponse();
        rateLimitingFilter.doFilter(mockRequest, mockResponse, chain);
        Assertions.assertEquals(429, response2.getStatus());
    }

}
