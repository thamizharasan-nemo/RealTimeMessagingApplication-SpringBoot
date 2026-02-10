package com.example.RealTimeChat.repository;

import com.example.RealTimeChat.model.FCMToken;
import com.example.RealTimeChat.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FCMTokenRepository extends JpaRepository<FCMToken, Integer> {
    List<FCMToken> findByUser_UserId(Integer userId);

    Optional<FCMToken> findByUser(User user);

    Optional<FCMToken> findByToken(String token);

    void deleteByToken(String token);

    @Query("SELECT ft FROM FCMToken ft WHERE ft.user IN :participants")
    List<FCMToken> findAllByUserIn(@Param("participants") List<User> participants);
}
