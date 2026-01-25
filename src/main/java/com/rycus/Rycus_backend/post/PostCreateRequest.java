package com.rycus.Rycus_backend.post;

public class PostCreateRequest {
    private String text;
    private String authorEmail;
    private String authorName;

    public PostCreateRequest() {}

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getAuthorEmail() { return authorEmail; }
    public void setAuthorEmail(String authorEmail) { this.authorEmail = authorEmail; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
}
