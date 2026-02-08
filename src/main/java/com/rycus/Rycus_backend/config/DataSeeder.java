package com.rycus.Rycus_backend.config;

import com.rycus.Rycus_backend.repository.UserRepository;
import com.rycus.Rycus_backend.user.PlanType;
import com.rycus.Rycus_backend.user.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;

@Configuration
@Profile({"local","dev"}) // ✅ IMPORTANTÍSIMO: NO correr en producción
public class DataSeeder {

    @Bean
    CommandLineRunner seedUsers(UserRepository userRepository,
                                PasswordEncoder passwordEncoder) {

        return args -> seed(userRepository, passwordEncoder);
    }

    @Transactional
    void seed(UserRepository repo, PasswordEncoder encoder) {

        seedVip(repo, encoder, "carretaph@gmail.com", "Carlos Carreta", "ADMIN");
        seedVip(repo, encoder, "mrs.stephanie22@gmail.com", "Stephanie VIP", "USER");
        seedVip(repo, encoder, "sraygadac@hotmail.com", "Sandra Raygada", "USER");

        System.out.println("✅ VIP users seeded");
    }

    private void seedVip(UserRepository repo,
                         PasswordEncoder encoder,
                         String email,
                         String name,
                         String role) {

        String e = email.trim().toLowerCase(Locale.ROOT);

        repo.findByEmailIgnoreCase(e).ifPresentOrElse(existing -> {

            // ✅ NO PISAR PASSWORD si ya es BCrypt
            String p = existing.getPassword();
            if (p == null || p.isBlank() || !p.startsWith("$2a$")) {
                existing.setPassword(encoder.encode("rycus123"));
                System.out.println("🛠 Fixed non-bcrypt password for: " + e);
            }

            // ✅ Normaliza datos importantes
            existing.setEmail(e);
            if (existing.getFullName() == null || existing.getFullName().isBlank()) {
                existing.setFullName(name);
            }
            existing.setRole(role);

            // ✅ Para tus VIP: FREE_LIFETIME (así no jode billing)
            existing.setPlanType(PlanType.FREE_LIFETIME);
            existing.setTrialEndsAt(null);
            existing.setSubscriptionEndsAt(Instant.now().plusSeconds(60L * 60 * 24 * 365 * 20)); // 20 años

            repo.save(existing);
            System.out.println("ℹ️ User already exists: " + e);

        }, () -> {
            User u = new User();
            u.setEmail(e);
            u.setFullName(name);
            u.setRole(role);

            // password temporal
            u.setPassword(encoder.encode("rycus123"));

            // VIP plan
            u.setPlanType(PlanType.FREE_LIFETIME);
            u.setTrialEndsAt(null);
            u.setSubscriptionEndsAt(Instant.now().plusSeconds(60L * 60 * 24 * 365 * 20));
            u.setFreeMonthsBalance(0);

            // avatar default
            u.setAvatarUrl(null);

            repo.save(u);
            System.out.println("👤 Created VIP user: " + e);
        });
    }
}
