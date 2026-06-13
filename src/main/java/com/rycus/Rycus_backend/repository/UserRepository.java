package com.rycus.Rycus_backend.repository;

import com.rycus.Rycus_backend.user.User;
import com.rycus.Rycus_backend.user.dto.UserMiniDto;
import com.rycus.Rycus_backend.user.dto.UserSearchDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // =========================================================
    // BASIC LOOKUPS
    // =========================================================

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String fullName,
            String email
    );

    // =========================================================
    // USERS SEARCH (LIVIANO)
    // /users/search?q=...
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
    // USERS SEARCH + REFERRAL FEE
    // /users/search-referrals?q=...
    // Busca en name/email/industry/city/state
    // =========================================================

    @Query("""
        select new com.rycus.Rycus_backend.user.dto.UserSearchDto(
            u.id,
            u.fullName,
            u.email,
            u.avatarUrl,
            u.offersReferralFee,
            u.referralFeeType,
            u.referralFeeValue,
            u.referralFeeNotes
        )
        from User u
        where (
            lower(coalesce(u.fullName, '')) like lower(concat('%', :q, '%'))
            or lower(coalesce(u.email, '')) like lower(concat('%', :q, '%'))
            or lower(coalesce(u.industry, '')) like lower(concat('%', :q, '%'))
            or lower(coalesce(u.city, '')) like lower(concat('%', :q, '%'))
            or lower(coalesce(u.state, '')) like lower(concat('%', :q, '%'))
            or lower(coalesce(u.serviceTerritory, '')) like lower(concat('%', :q, '%'))
        )
        order by u.offersReferralFee desc, coalesce(u.fullName, u.email) asc
    """)
    List<UserSearchDto> searchWithReferralFee(@Param("q") String q);

    // =========================================================
    // ADVANCED USERS SEARCH + REFERRAL FEE
    // /users/search-referrals/advanced?nameEmail=&industry=&location=
    // location busca city/state
    // =========================================================

    @Query("""
        select new com.rycus.Rycus_backend.user.dto.UserSearchDto(
            u.id,
            u.fullName,
            u.email,
            u.avatarUrl,
            u.offersReferralFee,
            u.referralFeeType,
            u.referralFeeValue,
            u.referralFeeNotes
        )
        from User u
        where (
            :nameEmail is null
            or :nameEmail = ''
            or lower(coalesce(u.fullName, '')) like lower(concat('%', :nameEmail, '%'))
            or lower(coalesce(u.email, '')) like lower(concat('%', :nameEmail, '%'))
        )
        and (
            :industry is null
            or :industry = ''
            or lower(coalesce(u.industry, '')) like lower(concat('%', :industry, '%'))
        )
        and (
                     :location is null
                     or :location = ''
                     or lower(coalesce(u.city, '')) like lower(concat('%', :location, '%'))
                     or lower(coalesce(u.state, '')) like lower(concat('%', :location, '%'))
                     or lower(coalesce(u.serviceTerritory, '')) like lower(concat('%', :location, '%'))
                 )
        order by u.offersReferralFee desc, coalesce(u.fullName, u.email) asc
    """)
    List<UserSearchDto> searchWithReferralFeeAdvanced(
            @Param("nameEmail") String nameEmail,
            @Param("industry") String industry,
            @Param("location") String location
    );

    // =========================================================
    // REFERRALS CORE
    // =========================================================

    boolean existsByReferralCodeIgnoreCase(String referralCode);

    Optional<User> findByReferralCodeIgnoreCase(String referralCode);

    long countByReferredByEmailIgnoreCase(String referredByEmail);

    // =========================================================
    // STRIPE (WEBHOOKS)
    // =========================================================

    Optional<User> findByStripeCustomerId(String stripeCustomerId);
}