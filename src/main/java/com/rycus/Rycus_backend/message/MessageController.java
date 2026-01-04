package com.rycus.Rycus_backend.message;

import com.rycus.Rycus_backend.message.dto.InboxThreadResponse;
import com.rycus.Rycus_backend.message.dto.MessageRequest;
import com.rycus.Rycus_backend.message.dto.MessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    // POST /messages
    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(@RequestBody MessageRequest request) {
        return ResponseEntity.ok(messageService.sendMessage(request));
    }

    // GET /messages/conversation?userEmail=...&otherUserEmail=...
    @GetMapping("/conversation")
    public ResponseEntity<List<MessageResponse>> getConversation(
            @RequestParam("userEmail") String userEmail,
            @RequestParam("otherUserEmail") String otherUserEmail
    ) {
        return ResponseEntity.ok(messageService.getConversation(userEmail, otherUserEmail));
    }

    // GET /messages/unread-count?userEmail=...
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@RequestParam("userEmail") String userEmail) {
        return ResponseEntity.ok(messageService.getUnreadCount(userEmail));
    }

    // PUT /messages/mark-read?userEmail=...&otherUserEmail=...
    @PutMapping("/mark-read")
    public ResponseEntity<Void> markConversationAsRead(
            @RequestParam("userEmail") String userEmail,
            @RequestParam("otherUserEmail") String otherUserEmail
    ) {
        messageService.markConversationAsRead(userEmail, otherUserEmail);
        return ResponseEntity.noContent().build();
    }

    // GET /messages/inbox?userEmail=...
    @GetMapping("/inbox")
    public ResponseEntity<List<InboxThreadResponse>> inbox(
            @RequestParam("userEmail") String userEmail
    ) {
        return ResponseEntity.ok(messageService.getInbox(userEmail));
    }
}
