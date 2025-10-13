package com.example.RealTimeChat.controller;

import com.example.RealTimeChat.DTO.PayloadDTO.UpdateProfilePicDTO;
import com.example.RealTimeChat.DTO.PayloadDTO.UpdateUserBioDTO;
import com.example.RealTimeChat.DTO.UserDTO;
import com.example.RealTimeChat.DTO.UserResponseDTO;
import com.example.RealTimeChat.model.User;
import com.example.RealTimeChat.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("/admin/all")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/admin/id/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable int userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserResponseDTO>> getAllUsersAsDTO() {
        return ResponseEntity.ok(userService.getAllUsersAsDTO());
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserResponseDTO>> getUsersByKeyword(@RequestParam String keyword) {
        return ResponseEntity.ok(userService.findByKeyword(keyword));
    }

    @GetMapping("/search-by")
    public ResponseEntity<List<UserResponseDTO>> getUsersByUsername(@RequestParam String username) {
        return ResponseEntity.ok(userService.findByUsername(username));
    }

    @GetMapping("/search-with")
    public ResponseEntity<List<UserResponseDTO>> getUsersByPhoneNo(@RequestParam String phoneNo) {
        return ResponseEntity.ok(userService.findByPhoneNo(phoneNo));
    }

    @GetMapping("/id/{userId}")
    public ResponseEntity<UserResponseDTO> getUserAsDTOById(@PathVariable int userId) {
        return ResponseEntity.ok(userService.getUserAsDTOById(userId));
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addUser(@Valid @ModelAttribute UserDTO userDTO) {
        return ResponseEntity.ok(userService.addUser(userDTO));
    }

    @PutMapping("/update/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable int userId, @Valid @ModelAttribute UserDTO userDTO) {
        return ResponseEntity.ok(userService.updateUser(userId, userDTO));

    }

    @PostMapping("/update/prof-pic")
    public ResponseEntity<?> updateProfPic(@ModelAttribute UpdateProfilePicDTO updateProfilePicDTO){
        userService.updateProfPic(updateProfilePicDTO);
        return ResponseEntity.ok().body("Profile pic updated successfully.");
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable int userId) {
        userService.deleteUserById(userId);
        return ResponseEntity.ok().body("User account deleted successfully");
    }

    @PostMapping("/update/bio")
    public ResponseEntity<?> updateUserBio(@RequestBody UpdateUserBioDTO userBioDTO) {
        userService.updateBio(userBioDTO);
        return ResponseEntity.ok().body("User bio updated successfully");
    }


}
