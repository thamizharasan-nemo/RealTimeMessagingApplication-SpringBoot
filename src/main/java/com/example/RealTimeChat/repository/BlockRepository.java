package com.example.RealTimeChat.repository;

import com.example.RealTimeChat.model.Block;
import com.example.RealTimeChat.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlockRepository extends JpaRepository<Block, Integer> {

    boolean existsByBlockerAndBlocked(User blocker, User blocked);

    @Query("SELECT b FROM Block b WHERE b.blocker.userId = :blockerId")
    List<Block> findAllBlockedByBlockerId(@Param("blockerId") int blockerId);

    @Query("DELETE FROM Block b WHERE b.blocker.userId = :blockerId AND b.blocked.userId = :blockedId")
    void deleteByBlockerAndBlocked(@Param("blockerId") int blockerId, @Param("blockedId") int blockedId);
}
