package com.rycus.Rycus_backend.post;

public class CommentCreateRequest {
    private String text;
    private String authorEmail;
    private String authorName;

    public CommentCreateRequest() {}

    public String getText() { return text; }
    public String getAuthorEmail() { return authorEmail; }
    public String getAuthorName() { return authorName; }

    public void setText(String text) { this.text = text; }
    public void setAuthorEmail(String authorEmail) { this.authorEmail = authorEmail; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
}