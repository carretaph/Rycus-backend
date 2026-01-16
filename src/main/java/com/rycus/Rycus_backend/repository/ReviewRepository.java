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
    // Reviews creados por un usuario (email)
    // =========================================
    List<Review> findByCreatedByIgnoreCase(String createdBy);

    // =========================================
    // (OPCIONAL) Anti-spam suave:
    // último review del mismo usuario para ese customer
    // 👉 sirve solo para evitar doble-click inmediato
    // 👉 NO bloquea múltiples reviews históricos
    // =========================================
    Optional<Review> findTopByCustomer_IdAndCreatedByIgnoreCaseOrderByCreatedAtDesc(
            Long customerId,
            String createdBy
    );

    // =========================================
    // ⭐ CLAVE DEL MILESTONE ⭐
    // Cuenta CUÁNTOS CUSTOMERS DISTINTOS
    // ha revieweado un usuario dentro
    // de una ventana de tiempo (promo 3 meses)
    //
    // ✔ cuenta customers creados por otros
    // ✔ cuenta customers creados por el mismo user
    // ✔ 1 customer = 1 punto (aunque tenga varios reviews)
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

    // ❌ ELIMINADO A PROPÓSITO
    // Esto era lo que causaba el 409
    // boolean existsByCreatedByIgnoreCaseAndCustomer_Id(String createdBy, Long customerId);
}
