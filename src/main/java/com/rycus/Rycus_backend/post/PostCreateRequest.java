package com.rycus.Rycus_backend.post;

public class PostCreateRequest {
    private String text;
    private String authorEmail;
    private String authorName;

    private Boolean officialPost;
    private Boolean pinned;
    private String imageUrl;

    public PostCreateRequest() {}

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getAuthorEmail() { return authorEmail; }
    public void setAuthorEmail(String authorEmail) { this.authorEmail = authorEmail; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public Boolean getOfficialPost() { return officialPost; }
    public void setOfficialPost(Boolean officialPost) { this.officialPost = officialPost; }

    public Boolean getPinned() { return pinned; }
    public void setPinned(Boolean pinned) { this.pinned = pinned; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}