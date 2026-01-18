package com.rycus.Rycus_backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    // 🔐 IMPORTANTE
    // Usa SIEMPRE el mismo secret en todo el backend
    // (login, validación, filtros)
    private static final String SECRET =
            "rycus_super_secret_key_change_this_please_123456";

    // =========================================================
    // Signing key
    // =========================================================
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(
                SECRET.getBytes(StandardCharsets.UTF_8)
        );
    }

    // =========================================================
    // ✅ GENERAR TOKEN (LOGIN)
    // =========================================================
    public String generateToken(String email) {

        long now = System.currentTimeMillis();

        // ⏳ duración del token (30 días)
        long expirationMs = 1000L * 60 * 60 * 24 * 30;

        return Jwts.builder()
                .setSubject(email.toLowerCase())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // =========================================================
    // Extraer email (subject)
    // =========================================================
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    // =========================================================
    // Validar token contra UserDetails
    // =========================================================
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return email != null
                && email.equalsIgnoreCase(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    // =========================================================
    // Expiración
    // =========================================================
    private boolean isTokenExpired(String token) {
        Date expiration = extractAllClaims(token).getExpiration();
        return expiration.before(new Date());
    }

    // =========================================================
    // Claims internos
    // =========================================================
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
