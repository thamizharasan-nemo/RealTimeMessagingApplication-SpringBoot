package com.example.RealTimeChat.security;

import com.example.RealTimeChat.exception.UserNotFoundException;
import com.example.RealTimeChat.model.User;
import com.example.RealTimeChat.repository.UserRepository;
import com.example.RealTimeChat.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepo;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepo = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.loadByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("You're not found in the database"));

        return new CustomUserDetails(user);
    }
}
