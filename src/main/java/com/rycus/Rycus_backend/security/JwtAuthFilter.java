package com.rycus.Rycus_backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();

        // Preflight
        if (HttpMethod.OPTIONS.matches(method)) return true;

        // ✅ NO JWT en auth (login/register/change-email/etc)
        if (path.startsWith("/auth/")) return true;

        // ✅ públicos
        if (path.equals("/") || path.equals("/ping") || path.equals("/health") || path.equals("/hello")) return true;
        if (path.equals("/actuator/health")) return true;

        // ✅ Stripe webhook público
        if (path.equals("/billing/webhook")) return true;

        // ✅ feed público
        if (HttpMethod.GET.matches(method) && path.equals("/posts/feed")) return true;

        // error explícito
        if (path.equals("/error")) return true;

        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // No hay header -> seguir (SecurityConfig decidirá)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();

        // Token vacío -> seguir
        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // ✅ CAMBIO CLAVE:
            // Tu JwtService NO tiene extractUsername(), usa el método que SÍ tienes.
            // Normalmente es extractEmail(token)
            String email = jwtService.extractEmail(token);  // <-- si tu método tiene otro nombre, renómbralo aquí

            // Si no pudimos sacar email -> seguir
            if (email == null || email.isBlank()) {
                filterChain.doFilter(request, response);
                return;
            }

            // Si ya hay auth en contexto, no re-auth
            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // ✅ Usa tu método existente
                if (jwtService.isTokenValid(token, userDetails)) {

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

        } catch (Exception ex) {
            // ✅ SUPER IMPORTANTE:
            // NO mandes 401 aquí. Si el token está malo, solo deja que continúe.
            // Así no rompes pantallas públicas / auth.
        }

        filterChain.doFilter(request, response);
    }
}
