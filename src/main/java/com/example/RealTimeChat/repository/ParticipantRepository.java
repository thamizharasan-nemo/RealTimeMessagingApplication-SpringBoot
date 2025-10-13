package com.example.RealTimeChat.repository;

import com.example.RealTimeChat.model.Conversation;
import com.example.RealTimeChat.model.ConversationParticipant;
import com.example.RealTimeChat.model.ParticipantRole;
import com.example.RealTimeChat.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ParticipantRepository extends JpaRepository<ConversationParticipant, Integer> {

    @Query("SELECT p FROM ConversationParticipant p WHERE p.conversation.conversationId = :conversationId")
    List<ConversationParticipant> findByConversationId(@Param("conversationId") int conversationId);

    @Query("SELECT p FROM ConversationParticipant p WHERE p.user.userId = :userId")
    Optional<ConversationParticipant> findByUserId(@Param("userId") int userId);

    Optional<ConversationParticipant> findByConversationAndUser(Conversation conversation, User user);

    @Query("SELECT cp FROM ConversationParticipant cp " +
            "WHERE cp.conversation.conversationId = :conversationId")
    Set<ConversationParticipant> findAllParticipantsByConversationId(@Param("conversationId") int conversationId);

    @Query("SELECT cp FROM ConversationParticipant cp " +
            "WHERE cp.conversation.conversationId = :conversationId " +
            "AND cp.user.userId = :userId")
    ConversationParticipant findByConversation_ConversationIdUser_UserId(@Param("conversationId") int conversationId,
                                                                         @Param("userId") int userId);

    @Query("SELECT COUNT(p) FROM ConversationParticipant p " +
            "WHERE p.conversation.conversationId = :conversationId " +
            "AND p.participantRole = :participantRole")
    int countNoOfAdminsInConversation(@Param("conversationId") int conversationId,
                                      @Param("participantRole") ParticipantRole participantRole);

    @Query("SELECT COUNT(p) FROM ConversationParticipant p " +
            "WHERE p.conversation.conversationId = :conversationId")
    int countNoOfParticipantsInConv(@Param("conversationId") int conversationId);

    @Query("SELECT COUNT(p) FROM ConversationParticipant p " +
            "WHERE p.conversation.conversationId = :conversationId " +
            "AND p.participantRole = ADMIN")
    int countNoOfAdminsInConv(@Param("conversationId") int conversationId);

    @Query("SELECT COUNT(p) FROM ConversationParticipant p " +
            "WHERE p.conversation.conversationId = :conversationId " +
            "AND p.participantRole = MEMBER")
    int getMembersCountInConv(@Param("conversationId") int conversationId);

    @Query("SELECT cp FROM ConversationParticipant cp " +
            "WHERE cp.user.userId = :userId AND cp.isFavorite = true")
    List<ConversationParticipant> findAllFavoritesByUserId(@Param("userId") int userId);

    @Query("SELECT cp FROM ConversationParticipant cp " +
            "WHERE cp.user.userId = :userId AND cp.isArchived = true")
    List<ConversationParticipant> findAllArchivedByUserId(@Param("userId") int userId);

    @Transactional
    @Modifying
    @Query("DELETE FROM ConversationParticipant p " +
            "WHERE p.conversation.conversationId = :conversationId " +
            "AND p.user.userId = :userId")
    void deleteByConversation_ConversationIdAndUser_UserId(@Param("conversationId") int conversationId,
                                                           @Param("userId") int userId);


}
