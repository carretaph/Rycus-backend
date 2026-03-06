package com.rycus.Rycus_backend.repository;

import com.rycus.Rycus_backend.post.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    List<PostImage> findByPostId(Long postId);

    void deleteByPostId(Long postId);
}