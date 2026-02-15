package com.rycus.Rycus_backend.repository;

import com.rycus.Rycus_backend.user.User;
import com.rycus.Rycus_backend.user.dto.UserMiniDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    // ✅ USERS SEARCH (para /users/search?q=... o query=...)
    // Devuelve lista liviana con avatarUrl
    // =========================================================
    @Query("""
        select new com.rycus.Rycus_backend.user.dto.UserMiniDto(
            u.id,
            u.fullName,
            u.email,
            u.avatarUrl
        )
        from User u
        where lower(coalesce(u.fullName, '')) like lower(concat('%', :q, '%'))
           or lower(coalesce(u.email, '')) like lower(concat('%', :q, '%'))
        order by coalesce(u.fullName, u.email) asc
    """)
    List<UserMiniDto> searchMini(@Param("q") String q);

    // =========================================================
    // ✅ REFERRALS
    // =========================================================

    boolean existsByReferralCodeIgnoreCase(String referralCode);

    Optional<User> findByReferralCodeIgnoreCase(String referralCode);

    long countByReferredByEmailIgnoreCase(String referredByEmail);

    // =========================================================
    // ✅ STRIPE (CLAVE PARA WEBHOOKS)
    // =========================================================

    Optional<User> findByStripeCustomerId(String stripeCustomerId);
}
