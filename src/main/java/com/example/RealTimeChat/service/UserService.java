package com.example.RealTimeChat.service;

import com.example.RealTimeChat.DTO.PayloadDTO.UpdateProfilePicDTO;
import com.example.RealTimeChat.DTO.PayloadDTO.UpdateUserBioDTO;
import com.example.RealTimeChat.DTO.UserDTO;
import com.example.RealTimeChat.DTO.UserResponseDTO;
import com.example.RealTimeChat.exception.BadRequestException;
import com.example.RealTimeChat.exception.UserNotFoundException;
import com.example.RealTimeChat.model.User;
import com.example.RealTimeChat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class UserService {

    @Autowired
    UserRepository userRepo;


    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public User getUserById(int userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found."));
    }

    public boolean isExists(int userId){
        return userRepo.existsById(userId);
    }

    public List<UserResponseDTO> findByUsername(String username) {
        List<User> users = userRepo.findByUsername(username);
        if(users.isEmpty()){
            throw new UserNotFoundException("User with " + username + " not found.");
        }
        return users.stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    public List<UserResponseDTO> findByPhoneNo(String phoneNo) {
        List<User> users = userRepo.findByPhoneNo(phoneNo);
        if(users.isEmpty()) {
            throw new UserNotFoundException("User with phone number" + phoneNo + " not found.");
        }
        return users.stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    public List<UserResponseDTO> findByKeyword(String keyword) {
        List<User> users = userRepo.findByKeyword(keyword);
        if(users.isEmpty()) {
            throw new UserNotFoundException("User with " + keyword + " not found.");
        }
        return users.stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    public User convertToUserFromDTO(User user, UserDTO userDTO) {
        user.setUsername(userDTO.getUsername());
        user.setPassword(userDTO.getPassword());
        user.setPhoneNo(userDTO.getPhoneNo());
        user.setNickname(userDTO.getNickname());
        user.setBio(userDTO.getBio().isEmpty() ? "Hey there! I'm using this app" : userDTO.getBio());
        try {
            user.setProfPic(userDTO.getImageFile().getBytes());
            user.setImageType(userDTO.getImageFile().getContentType());
            user.setProfPicName(userDTO.getImageFile().getOriginalFilename());
        } catch (IOException io){
            throw new RuntimeException("Error while uploading profile pic" + io.getMessage());
        }
        return user;
    }

    public UserResponseDTO convertToResponseDTO(User user) {
        UserResponseDTO responseDTO = new UserResponseDTO();
        responseDTO.setUserId(user.getUserId());
        responseDTO.setUsername(user.getUsername());
        responseDTO.setPassword(user.getPassword());
        responseDTO.setPhoneNo(user.getPhoneNo());
        responseDTO.setNickname(user.getNickname());
        responseDTO.setBio(user.getBio().isEmpty() ? "Hey there! I'm using this app" : user.getBio());
        responseDTO.setCreatedOn(user.getCreatedOn());
        responseDTO.setLastSeen(user.isOnline() ? "Online" : lastSeenHelper(user));
        return responseDTO;
    }

    public List<UserResponseDTO> getAllUsersAsDTO() {
        return userRepo.findAll().stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    public UserResponseDTO getUserAsDTOById(int userId) {
        return convertToResponseDTO(getUserById(userId));
    }

    @Transactional
    public UserResponseDTO addUser(UserDTO userDTO){

        if(userRepo.existsByUsername(userDTO.getUsername())){
            throw new BadRequestException("Username was taken! Try something new.");
        }
        // Creates user object only if the username is new
        if (userRepo.existsByPhoneNo(userDTO.getPhoneNo())) {
            throw new BadRequestException("This phone number is already registered.");
        }

        User user = new User();
        userRepo.save(convertToUserFromDTO(user, userDTO));
        return convertToResponseDTO(user);
    }

    @Transactional
    public UserResponseDTO updateUser(int userId, UserDTO userDTO){
        if(!userRepo.existsById(userId)){
            throw new UserNotFoundException("User with id " + userId+ " not found.");
        }

        if(userRepo.existsByUsernameAndUserIdNot(userDTO.getUsername(), userId)){
            throw new BadRequestException("Username was taken! Try something new.");
        }

        if(userRepo.existsByPhoneNoUserIdNot(userId, userDTO.getPhoneNo())){
            throw new BadRequestException("This phone number is already registered.");
        }

        User existedUser = getUserById(userId);
        userRepo.save(convertToUserFromDTO(existedUser, userDTO));
        return convertToResponseDTO(existedUser);
    }

    public void deleteUserById(int userId){
        if(getUserById(userId) == null){
            throw new UserNotFoundException("User Not Found!");
        }

        userRepo.deleteById(userId);
    }


    public String lastSeenHelper(User user) {
        Instant lastSeen = user.getLastSeen();
        long minutesAgo = Duration.between(Instant.now(), lastSeen).toMinutes();

        if (minutesAgo < 5) {
            return "last seen just now";
        } else if (minutesAgo < 60) {
            return "last seen " + minutesAgo + " minutes ago";
        } else if (minutesAgo < 720) {
            long hoursAgo = minutesAgo / 60;
            return "last seen " + hoursAgo + " hour ago";
        } else if (minutesAgo < 1440) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault());
            return "last seen at today " + formatter.format(lastSeen);
        } else if (minutesAgo > 1440 && minutesAgo < 2880) {
            return "last seen yesterday";
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d 'at' hh:mm a").withZone(ZoneId.systemDefault());
            return "last seen on " + formatter.format(lastSeen);
        }
    }

    public void updateProfPic(UpdateProfilePicDTO updatePicDTO) {
        User user = getUserById(updatePicDTO.getUserId());
        try {
            if (updatePicDTO.getFile() != null && !updatePicDTO.getFile().isEmpty()) {
                user.setProfPicName(updatePicDTO.getFile().getOriginalFilename());
                user.setImageType(updatePicDTO.getFile().getContentType());
                user.setProfPic(updatePicDTO.getFile().getBytes());
            }
        } catch (IOException io) {
            throw new RuntimeException("Error while uploading profile picture :" + io.getMessage());
        }
        userRepo.save(user);
    }

    public void updateBio(UpdateUserBioDTO userBioDTO){
        User user = getUserById(userBioDTO.getUserId());
        if(user == null){
            throw new UserNotFoundException("User with id " + userBioDTO.getUserId() + " not found.");
        }
        user.setBio(userBioDTO.getNewBio());
        userRepo.save(user);
    }
}
