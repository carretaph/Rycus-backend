package com.rycus.Rycus_backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
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
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final AppUserDetailsService appUserDetailsService;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, AppUserDetailsService appUserDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.appUserDetailsService = appUserDetailsService;
        System.out.println("✅ Loaded SecurityConfig (JWT enabled)");
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
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

        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:5174",
                "http://localhost",
                "https://rycus.app",
                "https://www.rycus.app",
                "capacitor://localhost",
                "ionic://localhost"
        ));

        config.setAllowedOriginPatterns(List.of(
                "http://192.168.*.*:5173",
                "http://10.*.*.*:5173",
                "http://172.16.*.*:5173",
                "http://172.17.*.*:5173",
                "http://172.18.*.*:5173",
                "http://172.19.*.*:5173",
                "http://172.20.*.*:5173",
                "http://172.21.*.*:5173",
                "http://172.22.*.*:5173",
                "http://172.23.*.*:5173",
                "http://172.24.*.*:5173",
                "http://172.25.*.*:5173",
                "http://172.26.*.*:5173",
                "http://172.27.*.*:5173",
                "http://172.28.*.*:5173",
                "http://172.29.*.*:5173",
                "http://172.30.*.*:5173",
                "http://172.31.*.*:5173",
                "https://*.vercel.app"
        ));

        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/",
                                "/ping",
                                "/auth/**",
                                "/public/**",
                                "/error"
                        ).permitAll()

                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/health/**",
                                "/actuator/info",
                                "/actuator/info/**"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET, "/posts/feed", "/posts/feed/**").permitAll()

                        // ✅ TEMPORAL: permitir backfill de geocoding
                        .requestMatchers("/customers/geocode-all", "/customers/*/geocode").permitAll()

                        .requestMatchers("/posts/**").authenticated()
                        .requestMatchers("/customers/**").authenticated()
                        .requestMatchers("/reviews/**").authenticated()
                        .requestMatchers("/messages/**").authenticated()
                        .requestMatchers("/users/**").authenticated()
                        .requestMatchers("/connections/**").authenticated()

                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}