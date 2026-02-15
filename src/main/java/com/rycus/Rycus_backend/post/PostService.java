package com.rycus.Rycus_backend.post;

import com.rycus.Rycus_backend.repository.PostLikeRepository;
import com.rycus.Rycus_backend.repository.PostRepository;
import com.rycus.Rycus_backend.repository.UserRepository;
import com.rycus.Rycus_backend.user.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository repo;
    private final PostLikeRepository likeRepo;

    // ✅ NUEVO
    private final UserRepository userRepo;

    public PostService(PostRepository repo, PostLikeRepository likeRepo, UserRepository userRepo) {
        this.repo = repo;
        this.likeRepo = likeRepo;
        this.userRepo = userRepo;
    }

    // =========================
    // CREATE POST
    // =========================
    public PostDto create(PostCreateRequest req) {
        String text = req.getText() == null ? "" : req.getText().trim();
        String email = req.getAuthorEmail() == null ? "" : req.getAuthorEmail().trim();
        String name = req.getAuthorName() == null ? "" : req.getAuthorName().trim();

        if (!StringUtils.hasText(text)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Text is required");
        }
        if (!StringUtils.hasText(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Author email is required");
        }
        if (!StringUtils.hasText(name)) {
            name = email.contains("@") ? email.split("@")[0] : email;
        }

        Post saved = repo.save(new Post(text, email, name));

        long count = 0L;
        boolean liked = false;

        // ✅ CAMBIO: usa toDto con avatar
        return toDto(saved, count, liked);
    }

    // =========================
    // FEED
    // viewerEmail opcional: si viene, calculamos likedByViewer
    // =========================
    public List<PostDto> feed(int limit, String viewerEmail) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        String viewer = viewerEmail == null ? "" : viewerEmail.trim();

        return repo.findAllByOrderByCreatedAtDesc(PageRequest.of(0, safeLimit))
                .stream()
                .map(p -> {
                    long count = likeRepo.countByPost(p);
                    boolean liked = StringUtils.hasText(viewer) && likeRepo.existsByPostAndUserEmail(p, viewer);

                    // ✅ CAMBIO: usa toDto con avatar
                    return toDto(p, count, liked);
                })
                .toList();
    }

    // =========================
    // LIKE
    // =========================
    public LikeStatusDto like(Long postId, String userEmail) {
        String email = userEmail == null ? "" : userEmail.trim();
        if (!StringUtils.hasText(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email is required");
        }

        Post post = repo.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        boolean already = likeRepo.existsByPostAndUserEmail(post, email);
        if (!already) {
            likeRepo.save(new PostLike(post, email));
        }

        long count = likeRepo.countByPost(post);
        return new LikeStatusDto(post.getId(), true, count);
    }

    // =========================
    // UNLIKE
    // =========================
    @Transactional
    public LikeStatusDto unlike(Long postId, String userEmail) {
        String email = userEmail == null ? "" : userEmail.trim();
        if (!StringUtils.hasText(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email is required");
        }

        Post post = repo.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        likeRepo.deleteByPostAndUserEmail(post, email);

        long count = likeRepo.countByPost(post);
        return new LikeStatusDto(post.getId(), false, count);
    }

    // =========================
    // HARD DELETE POST (only author)
    // DELETE /posts/{id}?email=...
    // =========================
    @Transactional
    public void deletePost(Long postId, String requesterEmail) {
        String email = requesterEmail == null ? "" : requesterEmail.trim();
        if (!StringUtils.hasText(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email is required");
        }

        Post post = repo.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        String authorEmail = post.getAuthorEmail() == null ? "" : post.getAuthorEmail().trim();
        if (!authorEmail.equalsIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the author can delete this post");
        }

        likeRepo.deleteByPost_Id(postId);
        repo.delete(post);
    }

    // =========================
    // EDIT POST (only author)
    // PUT /posts/{id}?email=...
    // =========================
    @Transactional
    public PostDto updatePost(Long postId, String requesterEmail, PostUpdateRequest req) {
        String email = requesterEmail == null ? "" : requesterEmail.trim();
        if (!StringUtils.hasText(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email is required");
        }

        String text = (req == null || req.getText() == null) ? "" : req.getText().trim();
        if (!StringUtils.hasText(text)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Text is required");
        }

        Post post = repo.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        String authorEmail = post.getAuthorEmail() == null ? "" : post.getAuthorEmail().trim();
        if (!authorEmail.equalsIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the author can edit this post");
        }

        post.setText(text);
        Post saved = repo.save(post);

        long count = likeRepo.countByPost(saved);
        boolean likedByViewer = likeRepo.existsByPostAndUserEmail(saved, email);

        // ✅ CAMBIO: usa toDto con avatar
        return toDto(saved, count, likedByViewer);
    }

    // =========================
    // ✅ MAPPER (AHORA CON AVATAR)
    // =========================
    private PostDto toDto(Post post, long likeCount, boolean likedByViewer) {

        String avatarUrl = null;

        String authorEmail = post.getAuthorEmail() == null ? "" : post.getAuthorEmail().trim();
        if (StringUtils.hasText(authorEmail)) {
            Optional<User> u = userRepo.findByEmailIgnoreCase(authorEmail);
            avatarUrl = u.map(User::getAvatarUrl).orElse(null);
        }

        return new PostDto(
                post.getId(),
                post.getText(),
                post.getAuthorEmail(),
                post.getAuthorName(),
                avatarUrl, // ✅ AQUI VA
                post.getCreatedAt(),
                likeCount,
                likedByViewer
        );
    }
}
