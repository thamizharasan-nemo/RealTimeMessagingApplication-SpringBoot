package com.example.RealTimeChat.repository;

import com.example.RealTimeChat.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Integer> {

    @Query("SELECT c FROM Conversation c " +
            "WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Conversation> findByConversationName(@Param("title") String title);

    // To return all message Ids in a conversation
    @Query("SELECT m.messageId FROM Conversation c " +
            "JOIN c.messages m " +
            "WHERE c.conversationId = :conversationId")
    List<Integer> findAllMessageIdsOfThisConvId(@Param("conversationId") int conversationId);

    @Query("SELECT COUNT(m) FROM Conversation c " +
            "LEFT JOIN c.messages m " +
            "WHERE c.conversationId = :conversationId " +
            "AND m.pinned = true ")
    Integer countNoOfPinnedMessageInConv(@Param("conversationId") int conversationId);

    @Query("SELECT c FROM Conversation c " +
            "LEFT JOIN  c.participants p " +
            "WHERE c.conversationType = 'PRIVATE' " +
            "AND SIZE(c.participants) = 1 " +
            "AND p.user.userId = :userId")
    Optional<Conversation> findSelfConversation(@Param("userId") int userId);

    @Query("SELECT c FROM Conversation c " +
            "LEFT JOIN c.participants p1 " +
            "LEFT JOIN c.participants p2 " +
            "WHERE c.conversationType = 'PRIVATE' " +
            "AND p1.user.userId = :userId1 " +
            "AND p2.user.userId = :userId2")
    Optional<Conversation> findPrivateConvBetweenTwoUsers(@Param("userId1") int userId1, @Param("userId2") int userId2);
}
