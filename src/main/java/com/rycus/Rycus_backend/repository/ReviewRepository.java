package com.rycus.Rycus_backend.repository;

import com.rycus.Rycus_backend.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // =========================================
    // Reviews de un customer, todos, más recientes primero
    // =========================================
    @Query("""
        SELECT r
        FROM Review r
        JOIN FETCH r.customer c
        WHERE c.id = :customerId
        ORDER BY r.createdAt DESC
    """)
    List<Review> findAllByCustomerIdFetchCustomer(
            @Param("customerId") Long customerId
    );

    // =========================================
    // Reviews de un customer creados por un usuario
    // =========================================
    @Query("""
        SELECT r
        FROM Review r
        JOIN FETCH r.customer c
        WHERE c.id = :customerId
          AND LOWER(r.createdBy) = LOWER(:email)
        ORDER BY r.createdAt DESC
    """)
    List<Review> findByCustomerIdAndCreatedByFetchCustomer(
            @Param("customerId") Long customerId,
            @Param("email") String email
    );

    // =========================================
    // Reviews creados por un usuario
    // =========================================
    List<Review> findByCreatedByIgnoreCase(String createdBy);

    // =========================================
    // Anti-spam: último review del mismo usuario para ese customer
    // =========================================
    Optional<Review> findTopByCustomer_IdAndCreatedByIgnoreCaseOrderByCreatedAtDesc(
            Long customerId,
            String createdBy
    );

    // =========================================
    // Milestone: customers distintos revieweados por usuario en ventana
    // =========================================
    @Query("""
        SELECT COUNT(DISTINCT r.customer.id)
        FROM Review r
        WHERE LOWER(r.createdBy) = LOWER(:email)
          AND r.createdAt >= :startAt
          AND r.createdAt < :endAt
    """)
    int countDistinctCustomersReviewedByUserInWindow(
            @Param("email") String email,
            @Param("startAt") OffsetDateTime startAt,
            @Param("endAt") OffsetDateTime endAt
    );

    // =========================================
    // Primer review del usuario
    // =========================================
    @Query("""
        SELECT MIN(r.createdAt)
        FROM Review r
        WHERE LOWER(r.createdBy) = LOWER(:email)
    """)
    Optional<OffsetDateTime> findFirstReviewAtByUser(@Param("email") String email);
}