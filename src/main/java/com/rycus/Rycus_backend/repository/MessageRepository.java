package com.rycus.Rycus_backend.repository;

import com.rycus.Rycus_backend.message.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // =========================================================
    // Conversación entre dos usuarios (orden cronológico)
    // Usado en:
    // - getConversation
    // - markConversationAsRead
    // =========================================================
    @Query("""
        SELECT m
        FROM Message m
        WHERE
            (LOWER(m.sender.email) = LOWER(:userEmail)
             AND LOWER(m.recipient.email) = LOWER(:otherUserEmail))
        OR
            (LOWER(m.sender.email) = LOWER(:otherUserEmail)
             AND LOWER(m.recipient.email) = LOWER(:userEmail))
        ORDER BY m.createdAt ASC
    """)
    List<Message> findConversation(
            @Param("userEmail") String userEmail,
            @Param("otherUserEmail") String otherUserEmail
    );

    // =========================================================
    // Unread count (badge)
    // Usado en:
    // - getUnreadCount
    // =========================================================
    long countByRecipient_EmailIgnoreCaseAndReadFlagFalse(String recipientEmail);

    // =========================================================
    // Inbox (todos los mensajes donde participo)
    // IMPORTANTE:
    // - Orden DESC para que el primer mensaje por thread sea el último
    // Usado en:
    // - getInbox
    // =========================================================
    @Query("""
        SELECT m
        FROM Message m
        WHERE
            LOWER(m.sender.email) = LOWER(:userEmail)
            OR LOWER(m.recipient.email) = LOWER(:userEmail)
        ORDER BY m.createdAt DESC
    """)
    List<Message> findAllForInbox(@Param("userEmail") String userEmail);
}
