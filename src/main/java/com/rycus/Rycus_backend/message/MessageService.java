package com.rycus.Rycus_backend.message;

import com.rycus.Rycus_backend.message.dto.InboxThreadResponse;
import com.rycus.Rycus_backend.message.dto.MessageRequest;
import com.rycus.Rycus_backend.message.dto.MessageResponse;
import com.rycus.Rycus_backend.repository.MessageRepository;
import com.rycus.Rycus_backend.repository.UserRepository;
import com.rycus.Rycus_backend.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public MessageService(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    // =========================================================
    // SEND (frontend manda emails)
    // body: { senderEmail, recipientEmail, content }
    // =========================================================
    @Transactional
    public MessageResponse sendMessage(MessageRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }

        String senderEmail = safe(request.getSenderEmail());
        String recipientEmail = safe(request.getRecipientEmail());
        String content = safe(request.getContent());

        if (senderEmail.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "senderEmail is required");
        if (recipientEmail.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "recipientEmail is required");
        if (content.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "content is required");

        User sender = userRepository.findByEmailIgnoreCase(senderEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender not found"));

        User recipient = userRepository.findByEmailIgnoreCase(recipientEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipient not found"));

        Message m = new Message();
        m.setSender(sender);
        m.setRecipient(recipient);
        m.setContent(content);
        m.setReadFlag(false);

        Message saved = messageRepository.save(m);
        return toDto(saved);
    }

    // =========================================================
    // Conversation
    // GET /messages/conversation?userEmail=...&otherUserEmail=...
    // =========================================================
    @Transactional(readOnly = true)
    public List<MessageResponse> getConversation(String userEmail, String otherUserEmail) {
        if (safe(userEmail).isBlank() || safe(otherUserEmail).isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userEmail and otherUserEmail are required");
        }

        return messageRepository.findConversation(userEmail, otherUserEmail)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // =========================================================
    // Unread count (badge)
    // GET /messages/unread-count?userEmail=...
    // =========================================================
    @Transactional(readOnly = true)
    public long getUnreadCount(String userEmail) {
        if (safe(userEmail).isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userEmail is required");
        }
        return messageRepository.countByRecipient_EmailIgnoreCaseAndReadFlagFalse(userEmail);
    }

    // =========================================================
    // Mark conversation as read
    // PUT /messages/mark-read?userEmail=...&otherUserEmail=...
    // =========================================================
    @Transactional
    public void markConversationAsRead(String userEmail, String otherUserEmail) {
        if (safe(userEmail).isBlank() || safe(otherUserEmail).isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userEmail and otherUserEmail are required");
        }

        List<Message> messages = messageRepository.findConversation(userEmail, otherUserEmail);

        boolean changed = false;
        for (Message m : messages) {
            if (m.getRecipient() != null
                    && m.getRecipient().getEmail() != null
                    && m.getRecipient().getEmail().equalsIgnoreCase(userEmail)
                    && !m.isReadFlag()) {
                m.setReadFlag(true);
                changed = true;
            }
        }

        if (changed) {
            messageRepository.saveAll(messages);
        }
    }

    // =========================================================
    // Inbox threads (1 por persona)
    // GET /messages/inbox?userEmail=...
    // =========================================================
    @Transactional(readOnly = true)
    public List<InboxThreadResponse> getInbox(String userEmail) {
        if (safe(userEmail).isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userEmail is required");
        }

        List<Message> all = messageRepository.findAllForInbox(userEmail);

        // key: otherEmail
        Map<String, InboxThreadResponse> threads = new LinkedHashMap<>();

        for (Message m : all) {
            if (m.getSender() == null || m.getRecipient() == null) continue;

            String senderEmail = safe(m.getSender().getEmail());
            String recipientEmail = safe(m.getRecipient().getEmail());

            boolean iAmSender = senderEmail.equalsIgnoreCase(userEmail);
            String otherEmail = iAmSender ? recipientEmail : senderEmail;
            User otherUser = iAmSender ? m.getRecipient() : m.getSender();

            InboxThreadResponse t = threads.get(otherEmail);
            if (t == null) {
                t = new InboxThreadResponse();
                t.setOtherEmail(otherEmail);
                t.setOtherUserId(otherUser.getId());

                String otherFullName = safe(otherUser.getFullName());
                t.setOtherFullName(otherFullName.isBlank() ? otherEmail : otherFullName);

                t.setUnreadCount(0);

                // all viene DESC => el primer msg visto del thread es el último
                t.setLastMessage(m.getContent());
                t.setLastMessageAt(m.getCreatedAt());
                t.setLastFromEmail(senderEmail);

                threads.put(otherEmail, t);
            }

            // unread: solo mensajes que ME LLEGARON y no leí
            if (!iAmSender && !m.isReadFlag()) {
                t.setUnreadCount(t.getUnreadCount() + 1);
            }
        }

        return new ArrayList<>(threads.values());
    }

    // =========================================================
    // DTO mapping
    // =========================================================
    private MessageResponse toDto(Message m) {
        MessageResponse dto = new MessageResponse();
        dto.setId(m.getId());

        if (m.getSender() != null) {
            dto.setSenderId(m.getSender().getId());
            dto.setSenderEmail(m.getSender().getEmail());
            dto.setSenderName(m.getSender().getFullName());
        }

        if (m.getRecipient() != null) {
            dto.setRecipientId(m.getRecipient().getId());
            dto.setRecipientEmail(m.getRecipient().getEmail());
            dto.setRecipientName(m.getRecipient().getFullName());
        }

        dto.setContent(m.getContent());
        dto.setRead(m.isReadFlag());
        dto.setCreatedAt(m.getCreatedAt());
        return dto;
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
