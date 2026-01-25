package com.rycus.Rycus_backend.post;

public class PostUpdateRequest {
    private String text;

    public PostUpdateRequest() {}

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
