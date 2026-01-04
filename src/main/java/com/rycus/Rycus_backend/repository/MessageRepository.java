package com.rycus.Rycus_backend.repository;

import com.rycus.Rycus_backend.message.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // Conversación entre 2 emails (ida/vuelta)
    @Query("""
            SELECT m FROM Message m
            WHERE (lower(m.sender.email) = lower(:userEmail) AND lower(m.recipient.email) = lower(:otherEmail))
               OR (lower(m.sender.email) = lower(:otherEmail) AND lower(m.recipient.email) = lower(:userEmail))
            ORDER BY m.createdAt ASC
           """)
    List<Message> findConversation(String userEmail, String otherEmail);

    // Inbox feed: todos los mensajes donde el usuario participa (para armar threads)
    @Query("""
            SELECT m FROM Message m
            WHERE lower(m.sender.email) = lower(:userEmail)
               OR lower(m.recipient.email) = lower(:userEmail)
            ORDER BY m.createdAt DESC
           """)
    List<Message> findAllForInbox(String userEmail);

    // Unread count (badge)
    long countByRecipient_EmailIgnoreCaseAndReadFlagFalse(String recipientEmail);
}
