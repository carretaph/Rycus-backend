package com.rycus.Rycus_backend.security;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        System.out.println("✅ Loaded SecurityConfig (JWT enabled)");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .securityMatcher("/**")
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                        (req, res, authEx) -> res.sendError(401, "Unauthorized")
                ))
                .authorizeHttpRequests(auth -> auth

                        // ✅ MUY IMPORTANTE: deja pasar errores internos (evita Whitelabel 401)
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()

                        // preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ✅ health / ping públicos
                        .requestMatchers(HttpMethod.GET,
                                "/",
                                "/ping",
                                "/health",
                                "/hello",
                                "/actuator/health"
                        ).permitAll()

                        // ✅ Stripe Webhook (Stripe NO manda JWT)
                        .requestMatchers(HttpMethod.POST, "/billing/webhook").permitAll()

                        // auth
                        .requestMatchers("/auth/**").permitAll()

                        // feed público
                        .requestMatchers(HttpMethod.GET, "/posts/feed").permitAll()

                        // 🧪 DEV: posts sin JWT
                        .requestMatchers(HttpMethod.POST, "/posts").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/posts/*").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/posts/*").permitAll()

                        // 🧪 DEV: likes sin JWT
                        .requestMatchers(HttpMethod.POST, "/posts/*/like").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/posts/*/like").permitAll()

                        // 🧪 DEV: avatar sin JWT
                        .requestMatchers(HttpMethod.POST, "/users/avatar").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/users/avatar").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/users/avatar").permitAll()

                        // 🧪 DEV: rehydrate user
                        .requestMatchers(HttpMethod.GET, "/users/me").permitAll()

                        // error explícito
                        .requestMatchers("/error").permitAll()

                        // 🔐 TODO lo demás requiere JWT
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "https://rycus.app",
                "https://www.rycus.app",
                "https://rycus-frontend.vercel.app",
                "http://localhost:5173"
        ));

        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
