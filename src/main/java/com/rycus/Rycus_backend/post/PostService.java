package com.rycus.Rycus_backend.post;

import com.cloudinary.Cloudinary;
import com.rycus.Rycus_backend.repository.*;
import com.rycus.Rycus_backend.user.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class PostService {

    private final PostRepository repo;
    private final PostLikeRepository likeRepo;
    private final UserRepository userRepo;
    private final PostCommentRepository commentRepo;
    private final PostImageRepository imageRepo;
    private final UserBlockRepository userBlockRepo;
    private final Cloudinary cloudinary;

    public PostService(
            PostRepository repo,
            PostLikeRepository likeRepo,
            UserRepository userRepo,
            PostCommentRepository commentRepo,
            PostImageRepository imageRepo,
            UserBlockRepository userBlockRepo,
            Cloudinary cloudinary
    ) {
        this.repo = repo;
        this.likeRepo = likeRepo;
        this.userRepo = userRepo;
        this.commentRepo = commentRepo;
        this.imageRepo = imageRepo;
        this.userBlockRepo = userBlockRepo;
        this.cloudinary = cloudinary;
    }

    // =====================================================
    // CREATE (JSON simple)
    // =====================================================
    public PostDto create(PostCreateRequest req) {

        String text = (req == null || req.getText() == null) ? "" : req.getText().trim();
        String email = (req == null || req.getAuthorEmail() == null) ? "" : req.getAuthorEmail().trim();
        String name = (req == null || req.getAuthorName() == null) ? "" : req.getAuthorName().trim();

        if (!StringUtils.hasText(text)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Text is required");
        }
        if (!StringUtils.hasText(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Author email is required");
        }
        if (!StringUtils.hasText(name)) {
            name = email.contains("@") ? email.split("@")[0] : email;
        }

        Post post = new Post(text, email, name);

// Solo admins pueden crear posts oficiales
        boolean isAdmin =
                email.equalsIgnoreCase("carretaph@gmail.com") ||
                        email.equalsIgnoreCase("carretaph@hotmail.com");

        if (isAdmin) {
            post.setOfficialPost(Boolean.TRUE.equals(req.getOfficialPost()));
            post.setPinned(Boolean.TRUE.equals(req.getPinned()));
            post.setImageUrl(req.getImageUrl());
            post.setVideoUrl(req.getVideoUrl());
        }

        Post saved = repo.save(post);
        return buildFullDto(saved, false);
    }

    // =====================================================
    // CREATE WITH IMAGES (MULTIPART)
    // =====================================================
    public PostDto createWithImages(
            String text,
            String email,
            String name,
            Boolean officialPost,
            Boolean pinned,
            List<MultipartFile> files
    ) {
        String safeText = text == null ? "" : text.trim();
        String safeEmail = email == null ? "" : email.trim();
        String safeName = name == null ? "" : name.trim();

        boolean isAdmin =
                safeEmail.equalsIgnoreCase("carretaph@gmail.com") ||
                        safeEmail.equalsIgnoreCase("carretaph@hotmail.com");

        String finalName = isAdmin && Boolean.TRUE.equals(officialPost)
                ? "Rycus Team"
                : safeName;

        if (!StringUtils.hasText(safeEmail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Author email is required");
        }
        if (!StringUtils.hasText(safeName)) {
            safeName = safeEmail.contains("@") ? safeEmail.split("@")[0] : safeEmail;
        }

        boolean hasFiles = files != null && !files.isEmpty();

        if (!StringUtils.hasText(safeText) && !hasFiles) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post cannot be empty");
        }

        if (hasFiles && files.size() > 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Max 6 images allowed");
        }

        Post post = new Post(safeText, safeEmail, finalName);

        if (isAdmin) {
            post.setOfficialPost(Boolean.TRUE.equals(officialPost));
            post.setPinned(Boolean.TRUE.equals(pinned));
        }

        Post saved = repo.save(post);

        int videoCount = 0;

        if (hasFiles) {
            for (MultipartFile f : files) {
                if (f == null || f.isEmpty()) continue;

                String contentType = f.getContentType() == null ? "" : f.getContentType().toLowerCase();

                if (contentType.startsWith("video/")) {
                    videoCount++;

                    if (videoCount > 1) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only 1 video allowed per post");
                    }

                    if (!isAdmin) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can upload videos");
                    }

                    String videoUrl = uploadPostVideo(f);
                    saved.setVideoUrl(videoUrl);
                    repo.save(saved);
                } else {
                    String imageUrl = uploadPostImage(f);
                    imageRepo.save(new PostImage(saved.getId(), imageUrl));
                }
            }
        }

        return buildFullDto(saved, false);
    }

    // =====================================================
    // FEED
    // =====================================================
    public List<PostDto> feed(int limit, String viewerEmail) {

        int safeLimit = Math.max(1, Math.min(limit, 100));
        String viewer = viewerEmail == null ? "" : viewerEmail.trim();

        User viewerUser = StringUtils.hasText(viewer)
                ? userRepo.findByEmailIgnoreCase(viewer).orElse(null)
                : null;

        return repo.findAllByOrderByPinnedDescCreatedAtDesc(PageRequest.of(0, safeLimit))
                .stream()
                .filter(p -> canViewerSeePost(viewerUser, p))
                .map(p -> {
                    boolean liked = StringUtils.hasText(viewer)
                            && likeRepo.existsByPostAndUserEmail(p, viewer);

                    return buildFullDto(p, liked);
                })
                .toList();
    }

    // =====================================================
    // LIKE
    // =====================================================
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

    // =====================================================
    // UNLIKE
    // =====================================================
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

    // =====================================================
    // UPDATE POST (only author)
    // =====================================================
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

        boolean likedByViewer = likeRepo.existsByPostAndUserEmail(saved, email);
        return buildFullDto(saved, likedByViewer);
    }

    // =====================================================
    // DELETE POST (only author)
    // =====================================================
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
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only author can delete");
        }

        likeRepo.deleteByPost_Id(postId);
        commentRepo.deleteByPostId(postId);
        imageRepo.deleteByPostId(postId);
        repo.delete(post);
    }

    // =====================================================
    // COMMENTS - LIST
    // =====================================================
    public List<CommentDto> listComments(Long postId, int limit) {

        int safeLimit = Math.max(1, Math.min(limit, 100));

        repo.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        return commentRepo.findByPostIdOrderByCreatedAtDesc(
                        postId,
                        PageRequest.of(0, safeLimit)
                )
                .stream()
                .map(this::toCommentDto)
                .toList();
    }

    // =====================================================
    // COMMENTS - ADD
    // =====================================================
    @Transactional
    public CommentDto addComment(Long postId, CommentCreateRequest req) {

        String text = (req == null || req.getText() == null) ? "" : req.getText().trim();
        String email = (req == null || req.getAuthorEmail() == null) ? "" : req.getAuthorEmail().trim();
        String name = (req == null || req.getAuthorName() == null) ? "" : req.getAuthorName().trim();

        if (!StringUtils.hasText(text)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Text is required");
        }
        if (!StringUtils.hasText(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Author email is required");
        }
        if (!StringUtils.hasText(name)) {
            name = email.contains("@") ? email.split("@")[0] : email;
        }

        repo.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        PostComment saved = commentRepo.save(new PostComment(postId, text, email, name));
        return toCommentDto(saved);
    }


    // =====================================================
    // HELPER: BLOCK FILTER
    // =====================================================
    private boolean canViewerSeePost(User viewerUser, Post post) {
        if (viewerUser == null || post == null || !StringUtils.hasText(post.getAuthorEmail())) {
            return true;
        }

        User author = userRepo.findByEmailIgnoreCase(post.getAuthorEmail())
                .orElse(null);

        if (author == null || author.getId() == null || viewerUser.getId() == null) {
            return true;
        }

        if (viewerUser.getId().equals(author.getId())) {
            return true;
        }

        boolean viewerBlockedAuthor =
                userBlockRepo.existsByBlockerAndBlocked(viewerUser, author);

        boolean authorBlockedViewer =
                userBlockRepo.existsByBlockerAndBlocked(author, viewerUser);

        return !viewerBlockedAuthor && !authorBlockedViewer;
    }

    // =====================================================
    // HELPER: BUILD FULL DTO
    // =====================================================
    private PostDto buildFullDto(Post post, boolean likedByViewer) {

        long likeCount = likeRepo.countByPost(post);
        long commentCount = commentRepo.countByPostId(post.getId());

        List<String> imageUrls = imageRepo.findByPostId(post.getId())
                .stream()
                .map(PostImage::getImageUrl)
                .toList();

        User author = userRepo.findByEmailIgnoreCase(post.getAuthorEmail())
                .orElse(null);

        String avatarUrl = author != null
                ? author.getAvatarUrl()
                : null;

        Long authorId = author != null
                ? author.getId()
                : null;

        PostDto dto = new PostDto(
                post.getId(),
                post.getText(),
                post.getAuthorEmail(),
                post.getAuthorName(),
                avatarUrl,
                post.getCreatedAt(),
                likeCount,
                likedByViewer,
                commentCount,
                imageUrls
        );

        dto.setAuthorId(authorId);
        dto.setOfficialPost(post.isOfficialPost());
        dto.setPinned(post.isPinned());
        dto.setImageUrl(post.getImageUrl());
        dto.setVideoUrl(post.getVideoUrl());

        return dto;
    }

    // =====================================================
    // COMMENT DTO MAPPER
    // =====================================================
    private CommentDto toCommentDto(PostComment c) {

        String avatarUrl = userRepo.findByEmailIgnoreCase(c.getAuthorEmail())
                .map(User::getAvatarUrl)
                .orElse(null);

        CommentDto dto = new CommentDto();
        dto.setId(c.getId());
        dto.setPostId(c.getPostId());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setAuthorName(c.getAuthorName());
        dto.setAuthorEmail(c.getAuthorEmail());
        dto.setAuthorAvatarUrl(avatarUrl);
        dto.setText(c.getText());
        return dto;
    }

    // =====================================================
    // CLOUDINARY UPLOAD
    // =====================================================
    private String uploadPostImage(MultipartFile file) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    Map.of(
                            "folder", "rycus/posts/images",
                            "resource_type", "image"
                    )
            );

            Object secureUrl = uploadResult.get("secure_url");
            if (secureUrl == null || !StringUtils.hasText(secureUrl.toString())) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Cloudinary did not return image secure_url"
                );
            }

            return secureUrl.toString();

        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to upload image"
            );
        }
    }

    private String uploadPostVideo(MultipartFile file) {

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    Map.of(
                            "folder", "rycus/posts/videos",
                            "resource_type", "video"
                    )
            );

            Object secureUrl = uploadResult.get("secure_url");
            if (secureUrl == null || !StringUtils.hasText(secureUrl.toString())) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Cloudinary did not return video secure_url"
                );
            }

            return secureUrl.toString();

        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to upload video"
            );
        }
    }

    // =====================================================
    // LEGACY DTO BUILDER (si aún lo usas)
    // =====================================================
    @SuppressWarnings("unused")
    private PostDto toDto(Post post, long likeCount, boolean likedByViewer) {

        String avatarUrl = userRepo.findByEmailIgnoreCase(post.getAuthorEmail())
                .map(User::getAvatarUrl)
                .orElse(null);

        return new PostDto(
                post.getId(),
                post.getText(),
                post.getAuthorEmail(),
                post.getAuthorName(),
                avatarUrl,
                post.getCreatedAt(),
                likeCount,
                likedByViewer
        );
    }
}