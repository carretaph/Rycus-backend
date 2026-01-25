package com.rycus.Rycus_backend.repository;

import com.rycus.Rycus_backend.post.Post;
import com.rycus.Rycus_backend.post.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByPostAndUserEmail(Post post, String userEmail);

    long countByPost(Post post);

    @Modifying
    @Transactional
    void deleteByPostAndUserEmail(Post post, String userEmail);

    // ✅ MÁS ROBUSTO: borrar likes por ID del post
    @Modifying
    @Transactional
    void deleteByPost_Id(Long postId);
}
