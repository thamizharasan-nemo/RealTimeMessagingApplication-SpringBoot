package com.example.RealTimeChat.repository;

import com.example.RealTimeChat.model.ReadReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReadReceiptRepository extends JpaRepository<ReadReceipt, Integer> {

    @Query("SELECT COUNT(r) > 0 FROM ReadReceipt r " +
            "WHERE r.message.messageId = :messageId " +
            "AND r.reader.userId = :readerId")
    boolean existsByMessageIdAndReaderId(@Param("messageId") int messageId, @Param("readerId") int readerId);

    @Query("SELECT r FROM ReadReceipt r WHERE r.message.messageId = :messageId")
    List<ReadReceipt> findAllReadersForMessage(@Param("messageId") int messageId);
}
