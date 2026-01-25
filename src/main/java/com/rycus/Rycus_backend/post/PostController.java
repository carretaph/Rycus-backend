package com.rycus.Rycus_backend.post;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
@CrossOrigin
public class PostController {

    private final PostService service;

    public PostController(PostService service) {
        this.service = service;
    }

    // =========================
    // CREATE POST
    // POST /posts
    // =========================
    @PostMapping
    public ResponseEntity<PostDto> create(@RequestBody PostCreateRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    // =========================
    // FEED
    // GET /posts/feed?limit=50&viewerEmail=...
    // =========================
    @GetMapping("/feed")
    public ResponseEntity<List<PostDto>> feed(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) String viewerEmail
    ) {
        return ResponseEntity.ok(service.feed(limit, viewerEmail));
    }

    // =========================
    // LIKE
    // POST /posts/{postId}/like?email=...
    // =========================
    @PostMapping("/{postId}/like")
    public ResponseEntity<LikeStatusDto> like(
            @PathVariable Long postId,
            @RequestParam("email") String email
    ) {
        return ResponseEntity.ok(service.like(postId, email));
    }

    // =========================
    // UNLIKE
    // DELETE /posts/{postId}/like?email=...
    // =========================
    @DeleteMapping("/{postId}/like")
    public ResponseEntity<LikeStatusDto> unlike(
            @PathVariable Long postId,
            @RequestParam("email") String email
    ) {
        return ResponseEntity.ok(service.unlike(postId, email));
    }

    // =========================
    // HARD DELETE POST (only author)
    // DELETE /posts/{postId}?email=...
    // =========================
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long postId,
            @RequestParam("email") String email
    ) {
        service.deletePost(postId, email);
        return ResponseEntity.noContent().build();
    }

    // =========================
    // EDIT POST (only author)
    // PUT /posts/{postId}?email=...
    // Body: { "text": "..." }
    // =========================
    @PutMapping("/{postId}")
    public ResponseEntity<PostDto> update(
            @PathVariable Long postId,
            @RequestParam("email") String email,
            @RequestBody PostUpdateRequest req
    ) {
        return ResponseEntity.ok(service.updatePost(postId, email, req));
    }
}
