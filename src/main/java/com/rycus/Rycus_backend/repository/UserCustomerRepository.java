package com.rycus.Rycus_backend.repository;

import com.rycus.Rycus_backend.customer.UserCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCustomerRepository extends JpaRepository<UserCustomer, Long> {

    // Para "My Customers" en orden (más nuevos primero)
    List<UserCustomer> findByUserEmailIgnoreCaseOrderByLinkedAtDesc(String userEmail);

    // Evita duplicados sin tener que pasar el objeto Customer
    boolean existsByUserEmailIgnoreCaseAndCustomer_Id(String userEmail, Long customerId);

    // Útil para upsert / debug
    Optional<UserCustomer> findByUserEmailIgnoreCaseAndCustomer_Id(String userEmail, Long customerId);
}
