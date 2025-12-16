package com.rycus.Rycus_backend.repository;

import com.rycus.Rycus_backend.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Reviews de un customer, más recientes primero
    // Spring Data interpreta customerId como review.customer.id
    List<Review> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
