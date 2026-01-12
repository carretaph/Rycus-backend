package com.rycus.Rycus_backend.repository;

import com.rycus.Rycus_backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmail(String email);

    // ✅ evita duplicados aunque cambien mayúsculas
    boolean existsByEmailIgnoreCase(String email);

    List<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String fullName,
            String email
    );

    // =========================================================
    // ✅ REFERRALS
    // =========================================================

    boolean existsByReferralCodeIgnoreCase(String referralCode);

    Optional<User> findByReferralCodeIgnoreCase(String referralCode);

    // Para “cada 5 referidos registrados…”
    long countByReferredByEmailIgnoreCase(String referredByEmail);
}
