package com.rycus.Rycus_backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final AppUserDetailsService appUserDetailsService;

    public SecurityConfig(
            JwtAuthFilter jwtAuthFilter,
            AppUserDetailsService appUserDetailsService
    ) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.appUserDetailsService = appUserDetailsService;

        System.out.println("✅ Loaded SecurityConfig (JWT enabled)");
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();

        provider.setUserDetailsService(appUserDetailsService);

        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of("*"));

        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS",
                "PATCH"
        ));

        config.setAllowedHeaders(List.of("*"));

        config.setExposedHeaders(List.of("Authorization"));

        config.setAllowCredentials(false);

        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .cors(Customizer.withDefaults())

                .csrf(csrf -> csrf.disable())

                .sessionManagement(sess ->
                        sess.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authenticationProvider(authenticationProvider())

                .authorizeHttpRequests(auth -> auth

                        // =========================================================
                        // PUBLIC ROUTES
                        // =========================================================
                        .requestMatchers(
                                "/",
                                "/ping",
                                "/auth/**",
                                "/public/**",
                                "/billing/**",
                                "/error"
                        ).permitAll()

                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/health/**",
                                "/actuator/info",
                                "/actuator/info/**"
                        ).permitAll()

                        // =========================================================
                        // PUBLIC GET ROUTES
                        // =========================================================
                        .requestMatchers(
                                HttpMethod.GET,
                                "/posts/feed",
                                "/posts/feed/**"
                        ).permitAll()

                        // =========================================================
                        // USERS PUBLIC SEARCH / PUBLIC PROFILE MEDIA
                        // =========================================================
                        .requestMatchers(
                                HttpMethod.GET,
                                "/users/search"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/users/all"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/customers/geocode-all"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/users/search-referrals/advanced"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/users/*/photos"
                        ).permitAll()

                        // =========================================================
                        // TEMP ADMIN TEST
                        // =========================================================
                        .requestMatchers(
                                HttpMethod.GET,
                                "/users/all"
                        ).permitAll()

                        // =========================================================
                        // ADMIN ROUTES
                        // =========================================================
                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")

                        // =========================================================
                        // PROTECTED ROUTES
                        // =========================================================
                        .requestMatchers("/posts/**").authenticated()

                        .requestMatchers("/customers/**").authenticated()

                        .requestMatchers("/reviews/**").authenticated()

                        .requestMatchers("/messages/**").authenticated()

                        .requestMatchers("/users/**").authenticated()

                        .requestMatchers("/connections/**").authenticated()

                        .anyRequest().authenticated()
                );

        http.addFilterBefore(
                jwtAuthFilter,
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }
}