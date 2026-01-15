package com.rycus.Rycus_backend.repository;

import com.rycus.Rycus_backend.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;

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

    // ❌ ELIMINADO A PROPÓSITO
    // Esto es lo que causaba el 409
    // boolean existsByCreatedByIgnoreCaseAndCustomer_Id(String createdBy, Long customerId);
}
