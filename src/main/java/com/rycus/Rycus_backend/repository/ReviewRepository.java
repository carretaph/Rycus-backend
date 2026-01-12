package com.rycus.Rycus_backend.repository;

import com.rycus.Rycus_backend.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Reviews de un customer, más recientes primero
    List<Review> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    // Reviews creados por un usuario (email), case-insensitive
    List<Review> findByCreatedByIgnoreCase(String createdBy);

    // ✅ Anti-trampa: ¿ya revisó este customer?
    boolean existsByCreatedByIgnoreCaseAndCustomer_Id(String createdBy, Long customerId);
}
