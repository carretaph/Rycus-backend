package com.rycus.Rycus_backend.repository;

import com.rycus.Rycus_backend.post.PostComment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    List<PostComment> findByPostIdOrderByCreatedAtDesc(Long postId, Pageable pageable);
    void deleteByPostId(Long postId);
    long countByPostId(Long postId);
}