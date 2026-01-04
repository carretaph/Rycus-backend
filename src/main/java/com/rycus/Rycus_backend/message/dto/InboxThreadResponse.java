package com.rycus.Rycus_backend.message.dto;

import java.time.LocalDateTime;

public class InboxThreadResponse {

    private Long otherUserId;
    private String otherFullName;
    private String otherEmail;

    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private String lastFromEmail;

    private int unreadCount;

    public Long getOtherUserId() { return otherUserId; }
    public void setOtherUserId(Long otherUserId) { this.otherUserId = otherUserId; }

    public String getOtherFullName() { return otherFullName; }
    public void setOtherFullName(String otherFullName) { this.otherFullName = otherFullName; }

    public String getOtherEmail() { return otherEmail; }
    public void setOtherEmail(String otherEmail) { this.otherEmail = otherEmail; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(LocalDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; }

    public String getLastFromEmail() { return lastFromEmail; }
    public void setLastFromEmail(String lastFromEmail) { this.lastFromEmail = lastFromEmail; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
}
