package com.rycus.Rycus_backend.post;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/posts")
@CrossOrigin
public class PostController {

    private final PostService service;

    public PostController(PostService service) {
        this.service = service;
    }

    // =====================================================
    // CREATE POST (JSON)
    // POST /posts
    // Content-Type: application/json
    // Body: { text, authorEmail, authorName }
    // =====================================================
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PostDto> create(@RequestBody PostCreateRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    // =====================================================
    // CREATE POST (MULTIPART + IMAGES)
    // POST /posts
    // Content-Type: multipart/form-data
    // Form fields:
    //   text (optional)
    //   authorEmail (required)
    //   authorName (required)
    //   files (optional, repeatable, max 6)
    // =====================================================
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PostDto> createWithImages(
            @RequestParam(value = "text", required = false) String text,
            @RequestParam("authorEmail") String authorEmail,
            @RequestParam("authorName") String authorName,
            @RequestParam(value = "files", required = false) List<MultipartFile> files
    ) {
        return ResponseEntity.ok(service.createWithImages(text, authorEmail, authorName, files));
    }

    // =====================================================
    // FEED
    // GET /posts/feed?limit=50&viewerEmail=...
    // =====================================================
    @GetMapping(value = "/feed", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PostDto>> feed(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) String viewerEmail
    ) {
        return ResponseEntity.ok(service.feed(limit, viewerEmail));
    }

    // =====================================================
    // LIKE
    // POST /posts/{postId}/like?viewerEmail=...   (frontend)
    // OR  /posts/{postId}/like?email=...         (legacy)
    // =====================================================
    @PostMapping(value = "/{postId}/like", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LikeStatusDto> like(
            @PathVariable Long postId,
            @RequestParam(value = "viewerEmail", required = false) String viewerEmail,
            @RequestParam(value = "email", required = false) String email
    ) {
        String who = (viewerEmail != null && !viewerEmail.trim().isEmpty())
                ? viewerEmail.trim()
                : (email == null ? "" : email.trim());

        return ResponseEntity.ok(service.like(postId, who));
    }

    // =====================================================
    // UNLIKE
    // DELETE /posts/{postId}/like?viewerEmail=...  (frontend)
    // OR     /posts/{postId}/like?email=...        (legacy)
    // =====================================================
    @DeleteMapping(value = "/{postId}/like", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LikeStatusDto> unlike(
            @PathVariable Long postId,
            @RequestParam(value = "viewerEmail", required = false) String viewerEmail,
            @RequestParam(value = "email", required = false) String email
    ) {
        String who = (viewerEmail != null && !viewerEmail.trim().isEmpty())
                ? viewerEmail.trim()
                : (email == null ? "" : email.trim());

        return ResponseEntity.ok(service.unlike(postId, who));
    }

    // =====================================================
    // DELETE POST (only author)
    // DELETE /posts/{postId}?viewerEmail=...  (frontend)
    // OR     /posts/{postId}?email=...        (legacy)
    // =====================================================
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long postId,
            @RequestParam(value = "viewerEmail", required = false) String viewerEmail,
            @RequestParam(value = "email", required = false) String email
    ) {
        String who = (viewerEmail != null && !viewerEmail.trim().isEmpty())
                ? viewerEmail.trim()
                : (email == null ? "" : email.trim());

        service.deletePost(postId, who);
        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // UPDATE POST (only author)
    // PUT /posts/{postId}?viewerEmail=...   (frontend)
    // OR  /posts/{postId}?email=...         (legacy)
    // Body: { "text": "..." }
    // =====================================================
    @PutMapping(value = "/{postId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PostDto> update(
            @PathVariable Long postId,
            @RequestParam(value = "viewerEmail", required = false) String viewerEmail,
            @RequestParam(value = "email", required = false) String email,
            @RequestBody PostUpdateRequest req
    ) {
        String who = (viewerEmail != null && !viewerEmail.trim().isEmpty())
                ? viewerEmail.trim()
                : (email == null ? "" : email.trim());

        return ResponseEntity.ok(service.updatePost(postId, who, req));
    }

    // =====================================================
    // COMMENTS - LIST
    // GET /posts/{postId}/comments?limit=50
    // =====================================================
    @GetMapping(value = "/{postId}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CommentDto>> listComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return ResponseEntity.ok(service.listComments(postId, limit));
    }

    // =====================================================
    // COMMENTS - ADD
    // POST /posts/{postId}/comments
    // Body: { text, authorEmail, authorName }
    // =====================================================
    @PostMapping(value = "/{postId}/comments", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommentDto> addComment(
            @PathVariable Long postId,
            @RequestBody CommentCreateRequest req
    ) {
        return ResponseEntity.ok(service.addComment(postId, req));
    }
}