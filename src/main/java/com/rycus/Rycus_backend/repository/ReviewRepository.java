package com.rycus.Rycus_backend.repository;

import com.rycus.Rycus_backend.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // =========================================
    // Reviews de un customer (más recientes primero)
    // =========================================
    List<Review> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    // =========================================
    // ✅ Reviews de un customer creados por un usuario
    // + JOIN FETCH customer para evitar LazyInitialization
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
    // Reviews creados por un usuario (email)
    // =========================================
    List<Review> findByCreatedByIgnoreCase(String createdBy);

    // =========================================
    // (OPCIONAL) Anti-spam suave:
    // último review del mismo usuario para ese customer
    // =========================================
    Optional<Review> findTopByCustomer_IdAndCreatedByIgnoreCaseOrderByCreatedAtDesc(
            Long customerId,
            String createdBy
    );

    // =========================================
    // ⭐ CLAVE DEL MILESTONE ⭐
    // Cuenta CUÁNTOS CUSTOMERS DISTINTOS ha revieweado el usuario
    // dentro de una ventana (promo 3 meses)
    // =========================================
    @Query(value = """
        SELECT COUNT(DISTINCT r.customer_id)
        FROM reviews r
        WHERE LOWER(r.created_by) = LOWER(:email)
          AND r.created_at >= :startAt
          AND r.created_at < :endAt
    """, nativeQuery = true)
    int countDistinctCustomersReviewedByUserInWindow(
            @Param("email") String email,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );

    // =========================================
    // ⭐ inicio real de la promo (primer review del usuario)
    // =========================================
    @Query("""
        SELECT MIN(r.createdAt)
        FROM Review r
        WHERE LOWER(r.createdBy) = LOWER(:email)
    """)
    Optional<LocalDateTime> findFirstReviewAtByUser(@Param("email") String email);
}
