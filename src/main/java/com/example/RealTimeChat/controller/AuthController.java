package com.example.RealTimeChat.controller;

import com.example.RealTimeChat.DTO.UserDTO;
import com.example.RealTimeChat.DTO.UserResponseDTO;
import com.example.RealTimeChat.model.User;
import com.example.RealTimeChat.security.CustomUserDetailsService;
import com.example.RealTimeChat.security.JwtService;
import com.example.RealTimeChat.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthController(UserService userService, AuthenticationManager authenticationManager, CustomUserDetailsService userDetailsService, JwtService jwtService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@Valid @ModelAttribute UserDTO userDTO){
        UserResponseDTO user = userService.addUser(userDTO);
        return ResponseEntity.ok(user);
    }


    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody UserDTO userDTO){

//        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//        System.out.println(passwordEncoder.encode("infinitevoid"));

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                userDTO.getUsername(),
                userDTO.getPassword()
        ));

        UserDetails userDetails = userDetailsService.loadUserByUsername(userDTO.getUsername());

        return ResponseEntity.ok(jwtService.generateToken(userDetails));
    }
}
