package com.example.eumbev2.common.security;

import com.example.eumbev2.entity.user.User;
import com.example.eumbev2.repository.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Reads the `Authorization: Bearer <token>` header, validates the access token, and (if valid)
 * populates the SecurityContext with a {@link UserPrincipal}. Invalid/missing tokens simply
 * proceed unauthenticated — the security filter chain decides whether that's acceptable for
 * the requested endpoint (permitAll vs. authenticated -> 401 via the entry point).
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        resolveToken(request).ifPresent(token -> {
            if (jwtTokenProvider.isValid(token)) {
                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                userRepository.findById(userId).ifPresent(this::authenticate);
            }
        });
        filterChain.doFilter(request, response);
    }

    private void authenticate(User user) {
        UserPrincipal principal = new UserPrincipal(user);
        var authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private Optional<String> resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HEADER);
        if (header != null && header.startsWith(PREFIX)) {
            return Optional.of(header.substring(PREFIX.length()));
        }
        return Optional.empty();
    }
}
