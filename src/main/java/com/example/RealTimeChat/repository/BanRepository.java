package com.example.RealTimeChat.repository;

import com.example.RealTimeChat.model.BannedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BanRepository extends JpaRepository<BannedUser, Integer> {

    @Query("SELECT COUNT(b) > 0 " +
            "FROM BannedUser b " +
            "WHERE b.conversation.conversationId = :convId " +
            "AND b.user.userId = :userId ")
    boolean existsByConvIdAndUserId(@Param("convId") int convId, @Param("userId") int userId);

    @Query("SELECT b FROM BannedUser b " +
            "WHERE b.conversation.conversationId = :convId " +
            "AND b.user.userId = :userId ")
    Optional<BannedUser> findByConversationIdAndUserId(@Param("convId") int convId, @Param("userId") int userId);
}
