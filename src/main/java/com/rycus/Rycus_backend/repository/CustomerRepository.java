package com.rycus.Rycus_backend.repository;

import com.rycus.Rycus_backend.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // ================================
    //  BÚSQUEDA POR CAMPOS ESPECÍFICOS
    // ================================
    List<Customer> findByFullNameContainingIgnoreCase(String fullName);
    List<Customer> findByCityContainingIgnoreCase(String city);
    List<Customer> findByCustomerTypeIgnoreCase(String customerType);

    // ================================
    //  DETECCIÓN / UPSERT GLOBAL
    // ================================
    Optional<Customer> findByEmailIgnoreCase(String email);

    Optional<Customer> findByFullNameIgnoreCaseAndPhone(String fullName, String phone);
    Optional<Customer> findByFullNameIgnoreCaseAndEmail(String fullName, String email);

    // ================================
    //  BÚSQUEDA GLOBAL (search bar)
    // ================================
    @Query("""
           SELECT c FROM Customer c
           WHERE (:text IS NULL OR
               LOWER(c.fullName)      LIKE LOWER(CONCAT('%', :text, '%')) OR
               LOWER(c.email)         LIKE LOWER(CONCAT('%', :text, '%')) OR
               LOWER(c.phone)         LIKE LOWER(CONCAT('%', :text, '%')) OR
               LOWER(c.address)       LIKE LOWER(CONCAT('%', :text, '%')) OR
               LOWER(c.city)          LIKE LOWER(CONCAT('%', :text, '%')) OR
               LOWER(c.state)         LIKE LOWER(CONCAT('%', :text, '%')) OR
               LOWER(c.zipCode)       LIKE LOWER(CONCAT('%', :text, '%')) OR
               LOWER(c.customerType)  LIKE LOWER(CONCAT('%', :text, '%')) OR
               LOWER(c.tags)          LIKE LOWER(CONCAT('%', :text, '%'))
           )
           """)
    List<Customer> searchByText(@Param("text") String text);

    // =========================================================
    //  🏆 MILESTONE
    //  Cuenta clientes DISTINTOS:
    //   - creados por este userId (Customer.createdByUserId)
    //   - que tengan al menos 1 review del mismo usuario (Review.createdBy = email)
    // =========================================================
    @Query("""
        SELECT COUNT(DISTINCT c.id)
        FROM Customer c
        JOIN Review r
        WHERE r.customer = c
          AND c.createdByUserId = :userId
          AND LOWER(r.createdBy) = LOWER(:userEmail)
    """)
    int countDistinctCustomersWithReviewByUser(
            @Param("userId") Long userId,
            @Param("userEmail") String userEmail
    );
}
