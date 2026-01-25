package com.rycus.Rycus_backend.repository;

import com.rycus.Rycus_backend.post.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
